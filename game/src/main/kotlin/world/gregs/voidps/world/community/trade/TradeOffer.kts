package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.community.trade.Trade.isTrading
import world.gregs.voidps.world.interact.dialogue.type.intEntry

/**
 * Offering an item to trade or loan
 */

val definitions: ItemDefinitions by inject()

// Item must be tradeable and not lent or a dummy item
val tradeRestriction = object : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        val def = definitions.get(id)
        return def.lendTemplateId != -1 || def.singleNoteTemplateId != -1 || def.dummyItem != 0 || !def["tradeable", true]
    }
}

on<Registered> { player: Player ->
    player.offer.itemRule = tradeRestriction
}

on<InterfaceOption>({ id == "trade_side" && component == "offer" }) { player: Player ->
    val amount = when (option) {
        "Offer" -> 1
        "Offer-5" -> 5
        "Offer-10" -> 10
        "Offer-All" -> Int.MAX_VALUE
        else -> return@on
    }
    offer(player, item.id, amount)
}

on<InterfaceOption>({ id == "trade_side" && component == "offer" && option == "Offer-X" }) { player: Player ->
    val amount = intEntry("Enter amount:")
    offer(player, item.id, amount)
}

on<InterfaceOption>({ id == "trade_side" && component == "offer" && option == "Value" }) { player: Player ->
    player.message("${item.def.name} is priceless!", ChatType.Trade)
}

fun offer(player: Player, id: String, amount: Int) {
    if (!isTrading(player, amount)) {
        return
    }
    val offered = player.inventory.transaction {
        val added = removeToLimit(id, amount)
        val transaction = link(player.offer)
        transaction.add(id, added)
    }
    if (!offered) {
        player.message("That item is not tradeable.")
    }
}