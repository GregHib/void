package content.skill.summoning

import content.entity.player.dialogue.type.intEntry
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
import world.gregs.voidps.engine.inv.moveAll
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit

private fun Player.familiarDef() = follower?.let { NPCDefinitions.get(it.id) }

val Player.beastOfBurdenCapacity: Int
    get() = familiarDef()?.get("summoning_beast_of_burden_capacity", 0) ?: 0

fun Player.hasBeastOfBurden(): Boolean = familiarDef()?.get("summoning_beast_of_burden", 0) == 1

fun Player.ensureBeastOfBurdenInventory() {
    val capacity = beastOfBurdenCapacity
    // A stale/undersized instance restored from an old save would have fewer
    // slots than the familiar's capacity. Discard it so the engine recreates it
    // from the definition at full size on next access. Only safe to discard when
    // empty, otherwise stored items would be lost.
    if (capacity > 0 && beastOfBurden.size < capacity && beastOfBurden.isEmpty()) {
        inventories.clear("beast_of_burden")
    }
}

fun Player.syncBeastOfBurdenInterface() {
    interfaceOptions.send("beast_of_burden", "items")
    sendInventory(beastOfBurden)
    sendInventory(inventory)
}

fun Player.openBeastOfBurden() {
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
    beastOfBurden.moveAll(inventory)
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
        FloorItems.add(tile, item.id, item.amount, owner = this)
    }
    beastOfBurden.clear()
    message("Your familiar's items have been dropped on the floor.")
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
        // Clamp to what's actually carried. moveToLimit's undo path (when the
        // source holds fewer than requested) re-shuffles items into the wrong
        // slots and leaves gaps, so never let it overshoot the source.
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
        player.ensureBeastOfBurdenInventory()
        val bob = player.beastOfBurden
        val usedSlots = bob.items.take(capacity).count { it.isNotEmpty() }
        if (usedSlots >= capacity && bob.indexOf(item.id) == -1) {
            player.message("Your familiar can't carry any more items.")
            return
        }
        // Clamp to what's actually held. moveToLimit's undo path (when the source
        // holds fewer than requested) re-shuffles items into the wrong slots and
        // leaves gaps in the beast of burden, so never let it overshoot the source.
        val toStore = minOf(amount, player.inventory.count(item.id))
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
            else -> player.syncBeastOfBurdenInterface()
        }
    }
}
