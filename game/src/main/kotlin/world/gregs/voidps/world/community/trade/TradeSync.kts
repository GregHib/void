package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.itemChange

/**
 * Persist updates on an offer to the other player
 */

val interfaceDefinitions: InterfaceDefinitions by inject()
val inventoryDefinitions: InventoryDefinitions by inject()

/*
    Offer
 */
itemChange("trade_offer") { player ->
    val other: Player = Trade.getPartner(player) ?: return@itemChange
    applyUpdates(other.otherOffer, this)
    val warn = player.hasRequest(other, "accept_trade") && removedAnyItems(this)
    if (warn) {
        highlightRemovedSlots(player, other, this)
    }
    modified(player, other, warn)
    updateValue(player, other)
}

fun highlightRemovedSlots(player: Player, other: Player, update: ItemChanged) {
    if (update.item.amount < update.fromItem.amount) {
        player.warn("trade_main", "offer_warning", update.index)
        other.warn("trade_main", "other_warning", update.index)
    }
}

fun Player.warn(id: String, componentId: String, slot: Int) {
    val component = interfaceDefinitions.getComponent(id, componentId) ?: return
    val inventory = inventoryDefinitions.get(component["inventory", ""])
    sendScript("trade_warning", (component["parent", -1] shl 16) or component.id, inventory["width", 0.0], inventory["height", 0.0], slot)
}

fun updateValue(player: Player, other: Player) {
    val value = player.offer.calculateValue().toInt()
    player["offer_value"] = value
    other["other_offer_value"] = value
}

/*
    Loan
 */
itemChange("item_loan") { player ->
    val other: Player = Trade.getPartner(player) ?: return@itemChange
    applyUpdates(other.otherLoan, this)
    val warn = player.hasRequest(other, "accept_trade") && removedAnyItems(this)
    modified(player, other, warn)
}

fun applyUpdates(inventory: Inventory, update: ItemChanged) {
    inventory.transaction { set(update.index, update.item, update.from, update.fromIndex) }
}

fun removedAnyItems(change: ItemChanged) = change.item.amount < change.fromItem.amount

fun modified(player: Player, other: Player, warned: Boolean) {
    if (warned) {
        player["offer_modified"] = true
        other["other_offer_modified"] = true
    }
    player.removeRequest(other, "accept_trade")
    player.interfaces.sendText("trade_main", "status", "")
    other.interfaces.sendText("trade_main", "status", "")
}

/*
    Item count
 */
itemChange("inventory") { player ->
    val other: Player = Trade.getPartner(player) ?: return@itemChange
    updateInventorySpaces(other, player)
}

fun updateInventorySpaces(player: Player, other: Player) {
    player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
}