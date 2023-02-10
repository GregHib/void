package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject

/**
 * Persist updates on an offer to the other player
 */

val interfaceDefinitions: InterfaceDefinitions by inject()
val containerDefinitions: ContainerDefinitions by inject()

/*
    Offer
 */
on<ItemChanged>({ container == "trade_offer" && it.contains("trade_partner") }) { player: Player ->
    val other: Player = Trade.getPartner(player) ?: return@on
    applyUpdates(other.otherOffer, this)
    val warn = player.hasRequest(other, "accept_trade") && removedAnyItems(this)
    if (warn) {
        highlightRemovedSlots(player, other, this)
    }
    modified(player, other, warn)
    updateValue(player, other)
}

fun highlightRemovedSlots(player: Player, other: Player, update: ItemChanged) {
    if (update.item.amount < update.oldItem.amount) {
        player.warn("trade_main", "offer_warning", update.index)
        other.warn("trade_main", "other_warning", update.index)
    }
}

fun Player.warn(id: String, component: String, slot: Int) {
    val comp = interfaceDefinitions.get(id).getComponentOrNull(component) ?: return
    val container = containerDefinitions.get(comp["container", ""])
    sendScript(143, (comp["parent", -1] shl 16) or comp.id, container["width", 0.0], container["height", 0.0], slot)
}

fun updateValue(player: Player, other: Player) {
    val value = player.offer.calculateValue().toInt()
    player.setVar("offer_value", value)
    other.setVar("other_offer_value", value)
}

/*
    Loan
 */
on<ItemChanged>({ container == "item_loan" && it.contains("trade_partner") }) { player: Player ->
    val other: Player = Trade.getPartner(player) ?: return@on
    applyUpdates(other.otherLoan, this)
    val warn = player.hasRequest(other, "accept_trade") && removedAnyItems(this)
    modified(player, other, warn)
}

fun applyUpdates(container: Container, update: ItemChanged) {
    container.transaction { set(update.index, update.item, update.from, update.to) }
}

fun removedAnyItems(change: ItemChanged) = change.item.amount < change.oldItem.amount

fun modified(player: Player, other: Player, warned: Boolean) {
    if (warned) {
        player.setVar("offer_modified", true)
        other.setVar("other_offer_modified", true)
    }
    player.removeRequest(other, "accept_trade")
    player.interfaces.sendText("trade_main", "status", "")
    other.interfaces.sendText("trade_main", "status", "")
}

/*
    Item count
 */
on<ItemChanged>({ container == "inventory" && it.contains("trade_partner") }) { player: Player ->
    val other: Player = Trade.getPartner(player) ?: return@on
    updateInventorySpaces(other, player)
}

fun updateInventorySpaces(player: Player, other: Player) {
    player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
}