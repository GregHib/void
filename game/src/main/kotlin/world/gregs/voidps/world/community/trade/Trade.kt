package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.getOrNull

object Trade {
    fun isTrading(player: Player, amount: Int): Boolean {
        if (player.action.type != ActionType.Trade) {
            return false
        }
        if (amount < 1) {
            return false
        }
        return true
    }

    fun getPartner(player: Player): Player? {
        val partner: Player? = player.getOrNull("trade_partner")
        if(partner == null) {
            player.action.cancel(ActionType.Trade)
        }
        return partner
    }
}

fun Container.calculateValue(): Long {
    var value = 0L
    for (item in getItems()) {
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

val Player.lent: Container
    get() = containers.container("lent_collection_box", false)

val Player.loan: Container
    get() = containers.container("item_loan", false)

val Player.otherLoan: Container
    get() = containers.container("item_loan", true)