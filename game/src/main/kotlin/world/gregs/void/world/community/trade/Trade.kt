package world.gregs.void.world.community.trade

import world.gregs.void.engine.action.ActionType
import world.gregs.void.engine.entity.character.contain.Container
import world.gregs.void.engine.entity.character.contain.container
import world.gregs.void.engine.entity.character.get
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.definition.ItemDefinitions

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

fun Container.calculateValue(decoder: ItemDefinitions): Long {
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