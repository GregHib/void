package content.skill.cooking

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.Bank
import content.entity.player.bank.bank
import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shift
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.timer.Timer

class Wine : Script {

    private val logger = InlineLogger()

    init {
        itemOnItem("grapes", "jug_of_water") { _, _ ->
            if (!has(Skill.Cooking, 35)) {
                message("You need a Cooking level of 35 to make Unfermented wine.")
                return@itemOnItem
            }

            val amount = minOf(inventory.count("grapes"), inventory.count("jug_of_water"))
            if (amount <= 1) {
                createWine(amount)
                return@itemOnItem
            }
            weakQueue("wine_creation_dialog") {
                val selected = makeAmount(
                    listOf("unfermented_wine"),
                    type = "Make",
                    maximum = amount,
                    text = "How many would you like to make?",
                ).second
                createWine(selected)
            }
        }

        inventoryUpdated("bank") { _, _ -> startFermentationIfNeeded(bank) }

        inventoryUpdated("inventory") { _, _ -> startFermentationIfNeeded(inventory) }

        dropped("unfermented_wine") {
            softTimers.clear("fermenting_wine")
            startFermentationIfNeeded(bank, inventory)
        }

        playerSpawn { startFermentationIfNeeded(bank, inventory) }

        timerStart("fermenting_wine") { 20 }

        timerTick("fermenting_wine") {
            val cookingLevel = levels.get(Skill.Cooking)
            val bankAmount = bank.count("unfermented_wine")
            ferment(bank, bankAmount, cookingLevel)
            if (bankAmount > 0) {
                sendScript("update_bank_slots")
            }
            ferment(inventory, inventory.count("unfermented_wine"), cookingLevel)
            if (bank.contains("unfermented_wine") || inventory.contains("unfermented_wine")) {
                Timer.CONTINUE
            } else {
                Timer.CANCEL
            }
        }
    }

    private fun Player.createWine(amount: Int) {
        if (amount <= 0) return

        weakQueue("wine_creation", 2) {
            val created = inventory.transaction {
                remove("grapes")
                remove("jug_of_water")
                add("unfermented_wine")
            }
            if (!created) return@weakQueue

            sound("squeeze_in")
            message("You squeeze the grapes into the jug. The wine begins to ferment.", ChatType.Filter)
            softTimers.clear("fermenting_wine")
            softTimers.start("fermenting_wine")
            createWine(amount - 1)
        }
    }

    private fun Player.startFermentationIfNeeded(vararg inventories: Inventory) {
        if (inventories.any { it.contains("unfermented_wine") }) {
            softTimers.startIfAbsent("fermenting_wine")
        }
    }

    private fun Player.ferment(
        inventory: Inventory,
        amount: Int,
        cookingLevel: Int,
    ) {
        if (amount <= 0) return

        if (inventory.id == "bank") {
            var remaining = amount
            val amountPerBatch = amount / 15
            repeat(15 + amount % 15) { index ->
                val batch = if (index < 15) amountPerBatch else 1
                if (batch > 0 && remaining > 0) {
                    remaining -= fermentBatch(inventory, minOf(batch, remaining), cookingLevel)
                }
            }
            val fallbackBatch = amountPerBatch.coerceAtLeast(1)
            while (remaining > 0) {
                val processed = fermentBatch(inventory, minOf(fallbackBatch, remaining), cookingLevel)
                if (processed == 0) break
                remaining -= processed
            }
            return
        }
        repeat(amount) { fermentBatch(inventory, 1, cookingLevel) }
    }

    private fun Player.fermentBatch(
        inventory: Inventory,
        amount: Int,
        cookingLevel: Int,
    ): Int {
        val wine = if (wineSuccess(cookingLevel)) "jug_of_wine" else "jug_of_bad_wine"
        val processed = if (inventory.id == "bank") {
            fermentBank(inventory, amount, wine)
        } else if (inventory.transaction { replace("unfermented_wine", wine) }) {
            1
        } else {
            0
        }
        if (processed > 0 && wine == "jug_of_wine") {
            exp(Skill.Cooking, 200.0 * processed)
        }
        return processed
    }

    private fun Player.fermentBank(
        inventory: Inventory,
        amount: Int,
        wine: String,
    ): Int {
        var remaining = amount
        var processed = 0
        while (remaining > 0) {
            val sourceIndex = inventory.indexOf("unfermented_wine")
            if (sourceIndex !in inventory.indices) break

            val sourceAmount = inventory[sourceIndex].amount
            val outputIndex = inventory.indexOf(wine)
            val outputCapacity = if (outputIndex == -1) {
                Int.MAX_VALUE
            } else {
                Int.MAX_VALUE - inventory[outputIndex].amount
            }
            val requestedAmount = if (inventory.isFull() && outputIndex == -1) {
                sourceAmount
            } else {
                minOf(remaining, sourceAmount)
            }
            val processAmount = minOf(requestedAmount, outputCapacity)
            if (processAmount <= 0) {
                break
            }

            val sourceTab = Bank.getTab(this, sourceIndex)
            val removed = inventory.transaction {
                remove(sourceIndex, "unfermented_wine", processAmount)
            }
            val added = removed &&
                inventory.transaction {
                    add(wine, processAmount)
                }
            if (!added) {
                if (removed) {
                    val restored = inventory.transaction {
                        add("unfermented_wine", processAmount)
                    }
                    if (!restored) {
                        logger.error {
                            "Failed to restore $processAmount unfermented wine for $this."
                        }
                    }
                }
                break
            }

            processed += processAmount
            remaining -= processAmount
            if (sourceIndex in inventory.indices && inventory[sourceIndex].isEmpty()) {
                val lastIndex = inventory.indices.lastOrNull { inventory[it].isNotEmpty() }
                if (lastIndex != null && lastIndex > sourceIndex) {
                    inventory.transaction {
                        shift(sourceIndex, lastIndex)
                    }
                }
                if (sourceTab > Bank.MAIN_TAB) Bank.decreaseTab(this, sourceTab)
            }
        }
        return processed
    }

    private fun wineSuccess(level: Int) = Level.success(level, 48..352)
}
