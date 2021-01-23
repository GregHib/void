package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.has
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRegistered
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.Trade.isTrading
import world.gregs.voidps.world.interact.dialogue.type.intEntry
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

/**
 * Offering an item to trade or loan
 */

val itemDecoder: ItemDefinitions by inject()

val lendable: (Int, Int) -> Boolean = { id, amount ->
    val def = itemDecoder.get(id)
    amount == 1 && def.lendId != -1
}

val tradeable: (Int, Int) -> Boolean = { id, _ ->
    val def = itemDecoder.get(id)
    def.notedTemplateId == -1 && def.lendTemplateId == -1 && def.singleNoteTemplateId == -1 && def.dummyItem == 0
}

PlayerRegistered then {
    player.loan.predicate = lendable
    player.offer.predicate = tradeable
}

InterfaceOption where { name == "trade_side" && component == "offer" } then {
    val amount = when(option) {
       "Offer" -> 1
        "Offer-5" -> 5
        "Offer-10" -> 10
        "Offer-All" -> Int.MAX_VALUE
        else -> return@then
    }
    offer(player, itemId, itemIndex, amount)
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Offer-X" } then {
    player.dialogue {
        val amount = intEntry("Enter amount:")
        offer(player, itemId, itemIndex, amount)
    }
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Value" } then {
    val item = itemDecoder.get(itemId)
    player.message("${item.name} is priceless!", ChatType.GameTrade)
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Lend" } then {
    val partner = getPartner(player) ?: return@then
    lend(player, partner, itemId, itemIndex)
}

fun offer(player: Player, id: Int, slot: Int, amount: Int) {
    if (!isTrading(player, amount)) {
        return
    }

    var amount = amount
    val currentAmount = player.inventory.getCount(id).toInt()
    if (amount > currentAmount) {
        amount = currentAmount
    }

    if(!player.inventory.move(player.offer, id, amount, slot)) {
        player.message("That item is not tradeable.")
    }
}

fun lend(player: Player, other: Player, id: Int, slot: Int) {
    if (!isTrading(player, 1)) {
        return
    }

    if(player.has("lent_item")) {
        player.message("You are already lending an item, you can't lend another.")
        return
    }

    if(other.has("borrowed_item")) {
        player.message("They are already borrowing an item and can't borrow another.")
        return
    }

    if(!player.inventory.move(player.loan, id, 1, slot)) {
        player.message("That item cannot be lent.")
    }
}