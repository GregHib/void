package content.social.trade

import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory

object Trade {

    fun isTradeInterface(id: String?) = id == "trade_main" || id == "trade_confirm"

    fun isTrading(player: Player, amount: Int): Boolean {
        val interfaceId = player.menu ?: return false
        if (!isTradeInterface(interfaceId)) {
            return false
        }
        if (amount < 1) {
            return false
        }
        return true
    }

    fun getPartner(player: Player): Player? {
        val partner: Player? = player["trade_partner"]
        if (partner == null && isTradeInterface(player.menu)) {
            player.closeMenu()
        }
        return partner
    }
}

fun Inventory.calculateValue(): Long {
    var value = 0L
    for (item in items) {
        if (item.isNotEmpty() && item.amount > 0) {
            value += (item.def.cost * item.amount)
        }
    }
    return value
}

val Player.offer: Inventory
    get() = inventories.inventory("trade_offer", false)

val Player.otherOffer: Inventory
    get() = inventories.inventory("trade_offer", true)

val Player.returnedItems: Inventory
    get() = inventories.inventory("returned_lent_items", false)

val Player.loan: Inventory
    get() = inventories.inventory("item_loan", false)

val Player.otherLoan: Inventory
    get() = inventories.inventory("item_loan", true)
