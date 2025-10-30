package content.social.trade

import content.entity.player.dialogue.type.intEntry
import content.social.trade.Trade.isTrading
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class TradeRemove : Script {

    init {
        interfaceOption(component = "offer_options", id = "trade_main") {
            val amount = when (option) {
                "Remove" -> 1
                "Remove-5" -> 5
                "Remove-10" -> 10
                "Remove-All" -> player.offer.count(item.id)
                "Remove-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            remove(player, item.id, itemSlot, amount)
        }

        interfaceOption("Value", "offer_options", "trade_main") {
            player.message("${item.def.name} is priceless!", ChatType.Trade)
        }

        interfaceOption("Remove", "loan_item", "trade_main") {
            removeLend(player, item.id, 0)
        }
    }

    /**
     * Removing an item from an offer or loan
     */

    fun remove(player: Player, id: String, slot: Int, amount: Int) {
        if (!isTrading(player, amount)) {
            return
        }
        player.offer.transaction {
            val added = link(player.inventory).addToLimit(id, amount)
            if (!inventory.stackable(id) && added == 1) {
                clear(slot)
            } else {
                removeToLimit(id, added)
            }
        }
    }

    fun removeLend(player: Player, id: String, slot: Int) {
        if (!isTrading(player, 1)) {
            return
        }
        player.loan.transaction {
            clear(slot)
            link(player.inventory).add(id, 1)
        }
    }
}
