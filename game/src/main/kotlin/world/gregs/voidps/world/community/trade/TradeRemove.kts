package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.community.trade.Trade.isTrading
import world.gregs.voidps.world.interact.dialogue.type.intEntry
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

/**
 * Removing an item from an offer or loan
 */

val itemDecoder: ItemDefinitions by inject()

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