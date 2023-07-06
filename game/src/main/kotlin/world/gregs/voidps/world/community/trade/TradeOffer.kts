package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.Trade.isTrading
import world.gregs.voidps.world.interact.dialogue.type.intEntry

/**
 * Offering an item to trade or loan
 */

val definitions: ItemDefinitions by inject()

val lendRestriction = object : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        return definitions.get(id).lendId == -1
    }
}

val tradeRestriction = object : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        val def = definitions.get(id)
        return def.notedTemplateId != -1 || def.lendTemplateId != -1 || def.singleNoteTemplateId != -1 || def.dummyItem != 0 || !def["tradeable", true]
    }
}

on<Registered> { player: Player ->
    player.loan.itemRule = lendRestriction
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

on<InterfaceOption>({ id == "trade_side" && component == "offer" && option == "Lend" }) { player: Player ->
    val partner = getPartner(player) ?: return@on
    lend(player, partner, item.id, itemSlot)
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

fun lend(player: Player, other: Player, id: String, slot: Int) {
    if (!isTrading(player, 1)) {
        return
    }

    if (player.contains("lent_item")) {
        player.message("You are already lending an item, you can't lend another.")
        return
    }

    if (other.contains("borrowed_item")) {
        player.message("They are already borrowing an item and can't borrow another.")
        return
    }

    val lent = player.inventory.transaction {
        swap(slot, player.loan, 0)
    }
    if (!lent) {
        player.message("That item cannot be lent.")
    } else {
        player["lend_time"] = 0
        other["other_lend_time"] = 0
    }
}