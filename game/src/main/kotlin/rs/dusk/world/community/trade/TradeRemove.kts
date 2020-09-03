package rs.dusk.world.community.trade

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.community.trade.Trade.isTrading
import rs.dusk.world.community.trade.loan
import rs.dusk.world.community.trade.offer
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * Removing an item from an offer or loan
 */

val itemDecoder: ItemDecoder by inject()

InterfaceOption where { name == "trade_main" && component == "offer_options" } then {
    val amount = when(option) {
        "Remove" -> 1
        "Remove-5" -> 5
        "Remove-10" -> 10
        "Remove-All" -> player.offer.getCount(itemId).toInt()
        else -> return@then
    }
    remove(player, itemId, itemIndex, amount)
}

InterfaceOption where { name == "trade_main" && component == "offer_options" && option == "Remove-X" } then {
    player.dialogue {
        val amount = intEntry("Enter amount:")
        remove(player, itemId, itemIndex, amount)
    }
}

InterfaceOption where { name == "trade_main" && component == "offer_options" && option == "Value" } then {
    val item = itemDecoder.get(itemId)
    player.message("${item.name} is priceless!", ChatType.GameTrade)
}

InterfaceOption where { name == "trade_main" && component == "loan_item" && option == "Remove" } then {
    removeLend(player, itemId, 0)
}

fun remove(player: Player, id: Int, slot: Int, amount: Int) {
    if (!isTrading(player, amount)) {
        return
    }
    player.offer.move(player.inventory, id, amount, slot)
}

fun removeLend(player: Player, id: Int, slot: Int) {
    if (!isTrading(player, 1)) {
        return
    }
    player.loan.move(player.inventory, id, 1, slot)
}