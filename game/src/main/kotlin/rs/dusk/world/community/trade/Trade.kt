package rs.dusk.world.community.trade

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player

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
        val partner: Player? = player["trade_partner"]
        if(partner == null) {
            player.action.cancel(ActionType.Trade)
        }
        return partner
    }
}
fun Container.calculateValue(decoder: ItemDecoder): Long {
    val items = getItems()
    val amounts = getAmounts()
    var value = 0L
    for ((index, item) in items.withIndex()) {
        val amount = amounts[index]
        if (item != -1 && amount > 0) {
            val itemDef = decoder.get(item)
            value += (itemDef.cost * amount)
        }
    }
    return value
}

val Player.offer: Container
    get() = container("trade_offer", false)

val Player.otherOffer: Container
    get() = container("trade_offer", true)

val Player.lent: Container
    get() = container("lent_collection_box", false)

val Player.loan: Container
    get() = container("item_loan", false)

val Player.otherLoan: Container
    get() = container("item_loan", true)