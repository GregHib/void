package content.social.trade

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*

class TradeSync : Script {

    val interfaceDefinitions: InterfaceDefinitions by inject()
    val inventoryDefinitions: InventoryDefinitions by inject()

    init {
        inventoryChanged("trade_offer") { player ->
            val other: Player = Trade.getPartner(player) ?: return@inventoryChanged
            applyUpdates(other.otherOffer, this)
            val warn = player.hasRequest(other, "accept_trade") && removedAnyItems(this)
            if (warn) {
                highlightRemovedSlots(player, other, this)
            }
            modified(player, other, warn)
            updateValue(player, other)
        }

        inventoryChanged("item_loan") { player ->
            val other: Player = Trade.getPartner(player) ?: return@inventoryChanged
            applyUpdates(other.otherLoan, this)
            val warn = player.hasRequest(other, "accept_trade") && removedAnyItems(this)
            modified(player, other, warn)
        }

        inventoryUpdate("inventory") { player ->
            val other: Player = Trade.getPartner(player) ?: return@inventoryUpdate
            updateInventorySpaces(other, player)
        }
    }

    /**
     * Persist updates on an offer to the other player
     */

    /*
        Offer
     */

    fun highlightRemovedSlots(player: Player, other: Player, update: InventorySlotChanged) {
        if (update.item.amount < update.fromItem.amount) {
            player.warn("trade_main", "offer_warning", update.index)
            other.warn("trade_main", "other_warning", update.index)
        }
    }

    fun Player.warn(id: String, componentId: String, slot: Int) {
        val component = interfaceDefinitions.getComponent(id, componentId) ?: return
        val inventory = inventoryDefinitions.get(component["inventory", ""])
        sendScript("trade_warning", component.id, inventory["width", 0.0], inventory["height", 0.0], slot)
    }

    fun updateValue(player: Player, other: Player) {
        val value = player.offer.calculateValue().toInt()
        player["offer_value"] = value
        other["other_offer_value"] = value
    }

    /*
        Loan
     */

    fun applyUpdates(inventory: Inventory, update: InventorySlotChanged) {
        inventory.transaction { set(update.index, update.item, update.from, update.fromIndex) }
    }

    fun removedAnyItems(change: InventorySlotChanged) = change.item.amount < change.fromItem.amount

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

    fun updateInventorySpaces(player: Player, other: Player) {
        player.interfaces.sendText("trade_main", "slots", "has ${other.inventory.spaces} free inventory slots.")
    }
}
