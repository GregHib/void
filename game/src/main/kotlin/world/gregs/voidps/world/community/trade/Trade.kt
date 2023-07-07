package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.entity.character.player.Player

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
        val partner: Player? = player.getOrNull("trade_partner")
        if (partner == null) {
            player.closeMenu()
        }
        return partner
    }
}

fun Container.calculateValue(): Long {
    var value = 0L
    for (item in items) {
        if (item.isNotEmpty() && item.amount > 0) {
            value += (item.def.cost * item.amount)
        }
    }
    return value
}

val Player.offer: Container
    get() = containers.container("trade_offer", false)

val Player.otherOffer: Container
    get() = containers.container("trade_offer", true)

val Player.returnedItems: Container
    get() = containers.container("returned_lent_items", false)

val Player.loan: Container
    get() = containers.container("item_loan", false)

val Player.otherLoan: Container
    get() = containers.container("item_loan", true)