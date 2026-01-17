package content.social.trade

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.inv.*

class TradeSync(val interfaceDefinitions: InterfaceDefinitions, val inventoryDefinitions: InventoryDefinitions) : Script {

    init {
        slotChanged("trade_offer") {
            val other: Player = Trade.getPartner(this) ?: return@slotChanged
            applyUpdates(other.otherOffer, it)
            val warn = hasRequest(other, "accept_trade") && removedAnyItems(it)
            if (warn) {
                highlightRemovedSlots(this, other, it)
            }
            modified(this, other, warn)
            updateValue(this, other)
        }

        slotChanged("item_loan") {
            val other: Player = Trade.getPartner(this) ?: return@slotChanged
            applyUpdates(other.otherLoan, it)
            val warn = hasRequest(other, "accept_trade") && removedAnyItems(it)
            modified(this, other, warn)
        }

        /**
         * Persist updates on an offer to the other player
         */
        inventoryUpdated("inventory") { _, _ ->
            val other: Player = Trade.getPartner(this) ?: return@inventoryUpdated
            updateInventorySpaces(other, this)
        }
    }

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
