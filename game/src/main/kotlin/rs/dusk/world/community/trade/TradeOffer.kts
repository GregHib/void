import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.community.trade.loan
import rs.dusk.world.community.trade.offer
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * Offering an item to trade or loan
 */

val itemDecoder: ItemDecoder by inject()

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Offer" } then {
    offer(player, itemId, itemIndex, 1)
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Offer-5" } then {
    offer(player, itemId, itemIndex, 5)
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Offer-10" } then {
    offer(player, itemId, itemIndex, 10)
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Offer-X" } then {
    player.dialogue {
        val amount = intEntry("Enter amount:")
        offer(player, itemId, itemIndex, amount)
    }
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Offer-All" } then {
    val amount = player.inventory.getAmount(itemIndex)// TODO count not amount
    offer(player, itemId, itemIndex, amount)
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Value" } then {
    val item = itemDecoder.get(itemId)
    player.message("${item.name} is priceless!", ChatType.GameTrade)
}

InterfaceOption where { name == "trade_side" && component == "offer" && option == "Lend" } then {
    lend(player, itemId, itemIndex)
}

fun offer(player: Player, id: Int, slot: Int, amount: Int) {
    if (!valid(player, amount)) {
        return
    }
    // TODO can item be traded
    player.inventory.move(player.offer, id, amount, slot)
}

fun lend(player: Player, id: Int, slot: Int) {
    if (!valid(player, 1)) {
        return
    }
    // TODO is item loanable
    player.inventory.move(player.loan, id, 1, slot)
}

fun valid(player: Player, amount: Int): Boolean {
    if (player.action.type != ActionType.Trade) {
        return false
    }
    if (amount < 1) {
        return false
    }
    return true
}