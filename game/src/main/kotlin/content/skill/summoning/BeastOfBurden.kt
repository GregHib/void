package content.skill.summoning

import content.entity.combat.underAttack
import content.entity.player.dialogue.type.intEntry
import content.entity.player.inv.item.tradeable
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveAllToLimit
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

private fun Player.familiarDef() = follower?.let { NPCDefinitions.get(it.id) }

val Player.beastOfBurdenCapacity: Int
    get() = familiarDef()?.get("summoning_beast_of_burden_capacity", 0) ?: 0

fun Player.hasBeastOfBurden(): Boolean = familiarDef()?.get("summoning_beast_of_burden", 0) == 1

fun Player.ensureBeastOfBurdenInventory() {
    val capacity = beastOfBurdenCapacity
    if (capacity > 0 && beastOfBurden.size < capacity && beastOfBurden.isEmpty()) {
        inventories.clear("beast_of_burden")
    }
}

fun Player.syncBeastOfBurdenInterface() {
    interfaceOptions.send("beast_of_burden", "items")
    sendInventory(beastOfBurden)
    sendInventory(inventory)
}

/**
 * Reorganises the stored items so they fill the interface from the top, leaving
 * any empty slots at the bottom. Order is preserved.
 */
fun Player.compactBeastOfBurden() {
    beastOfBurden.transaction {
        var target = 0
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isEmpty()) {
                continue
            }
            if (index != target) {
                set(target, item)
                set(index, null)
            }
            target++
        }
    }
}

fun Player.openBeastOfBurden() {
    if (underAttack) {
        message("You can't do that in combat.")
        return
    }
    if (!hasBeastOfBurden()) {
        message("Your follower can't carry any items.")
        return
    }
    ensureBeastOfBurdenInventory()
    interfaces.open("beast_of_burden")
    open("summoning_side")
    tab(Tab.Inventory)
    syncBeastOfBurdenInterface()
    interfaceOptions.unlockAll("beast_of_burden", "items", 0 until beastOfBurdenCapacity)
    interfaceOptions.unlockAll("summoning_side", "inventory", 0 until 28)
}

fun Player.takeAllBeastOfBurden() {
    if (!hasBeastOfBurden()) {
        message("Your follower can't carry any items.")
        return
    }
    if (beastOfBurden.isEmpty()) {
        message("Your familiar is not carrying any items.")
        return
    }
    val target = inventory
    beastOfBurden.transaction {
        moveAllToLimit(target)
    }
    syncBeastOfBurdenInterface()
    if (!beastOfBurden.isEmpty()) {
        inventoryFull()
    }
}

fun Player.dropBeastOfBurdenItems() {
    if (beastOfBurden.isEmpty()) {
        return
    }
    for (item in beastOfBurden.items) {
        if (item.isEmpty()) {
            continue
        }
        // Owner-only, retrievable for five minutes before despawning.
        FloorItems.add(tile, item.id, item.amount, revealTicks = FloorItems.NEVER, disappearTicks = TimeUnit.MINUTES.toTicks(5), owner = this)
    }
    beastOfBurden.clear()
    message("Your familiar has dropped all the items it was holding.")
}

class BeastOfBurden : Script {

    init {
        npcOperate("Store", "*_familiar") { (target) ->
            if (target != follower) {
                message("That's not your familiar.")
                return@npcOperate
            }
            openBeastOfBurden()
        }

        npcOperate("Interact", "*_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            updateFamiliarInterface()
        }

        interfaceOpened("beast_of_burden") { id ->
            if (!hasBeastOfBurden()) {
                interfaces.close("beast_of_burden")
                return@interfaceOpened
            }
            ensureBeastOfBurdenInventory()
            compactBeastOfBurden()
            open("summoning_side")
            tab(Tab.Inventory)
            syncBeastOfBurdenInterface()
            interfaceOptions.unlockAll(id, "items", 0 until beastOfBurdenCapacity)
        }

        interfaceClosed("beast_of_burden") {
            close("summoning_side")
            sendScript("clear_dialogues")
            open("inventory")
        }

        interfaceOpened("summoning_side") { id ->
            interfaceOptions.send(id, "inventory")
            interfaceOptions.unlockAll(id, "inventory", 0 until 28)
            sendInventory(inventory)
        }

        interfaceClosed("summoning_side") {
            open("inventory")
        }

        interfaceOption(id = "beast_of_burden:items") { (item, _, option) ->
            val amount = when (option) {
                "Withdraw-1" -> 1
                "Withdraw-5" -> 5
                "Withdraw-10" -> 10
                "Withdraw-All" -> beastOfBurden.count(item.id)
                "Withdraw-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            withdraw(this, item, amount)
        }

        interfaceOption(id = "summoning_side:inventory") { (item, _, option) ->
            val amount = when (option) {
                "Store-1" -> 1
                "Store-5" -> 5
                "Store-10" -> 10
                "Store-All" -> inventory.count(item.id)
                "Store-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            store(this, item, amount)
        }

        interfaceOption("Take BoB", "beast_of_burden:take_bob") {
            takeAllBeastOfBurden()
        }

        interfaceOption("Take BoB", "familiar_details:take_bob_items") {
            takeAllBeastOfBurden()
        }

        interfaceOption("Take BoB", "summoning_orb:*take_bob") {
            takeAllBeastOfBurden()
        }
    }

    private fun withdraw(player: Player, item: world.gregs.voidps.engine.entity.item.Item, amount: Int) {
        if (amount < 1) {
            return
        }
        val toWithdraw = minOf(amount, player.beastOfBurden.count(item.id))
        if (toWithdraw < 1) {
            return
        }
        player.beastOfBurden.transaction {
            moveToLimit(item.id, toWithdraw, player.inventory)
        }
        when (player.beastOfBurden.transaction.error) {
            is TransactionError.Full -> player.inventoryFull()
            else -> player.syncBeastOfBurdenInterface()
        }
    }

    private fun store(player: Player, item: world.gregs.voidps.engine.entity.item.Item, amount: Int) {
        if (amount < 1) {
            return
        }
        if (!player.hasBeastOfBurden()) {
            player.message("Your follower can't carry any items.")
            return
        }
        val capacity = player.beastOfBurdenCapacity
        if (capacity <= 0) {
            player.message("Your follower can't carry any items.")
            return
        }
        // Familiars only carry tradeable items (the same rule the trade screen uses).
        val def = item.def
        if (def.lendTemplateId != -1 || def.dummyItem != 0 || !item.tradeable) {
            player.message("Your familiar can't carry that item.")
            return
        }
        player.ensureBeastOfBurdenInventory()
        val bob = player.beastOfBurden
        val usedSlots = bob.items.count { it.isNotEmpty() }
        val sharesStack = bob.stackable(item.id) && bob.indexOf(item.id) != -1
        val freeSlots = capacity - usedSlots
        if (freeSlots <= 0 && !sharesStack) {
            player.message("Your familiar can't carry any more items.")
            return
        }
        val requested = minOf(amount, player.inventory.count(item.id))
        var toStore = requested
        if (!sharesStack && !bob.stackable(item.id)) {
            // Each non-stackable item needs its own slot, so cap to the free slots.
            toStore = minOf(toStore, freeSlots)
        }
        if (toStore < 1) {
            return
        }
        player.inventory.transaction {
            val moved = moveToLimit(item.id, toStore, player.beastOfBurden)
            if (moved == 0) {
                error = TransactionError.Full()
            }
        }
        when (player.inventory.transaction.error) {
            is TransactionError.Full -> player.message("Your familiar can't carry any more items.")
            else -> {
                if (toStore < requested) {
                    player.message("Your familiar can't carry any more items.")
                }
                player.syncBeastOfBurdenInterface()
            }
        }
    }
}
