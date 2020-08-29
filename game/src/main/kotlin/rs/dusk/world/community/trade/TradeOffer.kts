import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.community.trade.Trade.isValidAmount
import rs.dusk.world.community.trade.loan
import rs.dusk.world.community.trade.offer
import rs.dusk.world.interact.dialogue.type.intEntry
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * Offering an item to trade or loan
 */

val itemDecoder: ItemDecoder by inject()

InterfaceOption where { name == "trade_side" && component == "offer" } then {
    val amount = when(option) {
       "Offer" -> 1
        "Offer-5" -> 5
        "Offer-10" -> 10
        "Offer-All" -> player.inventory.getCount(itemId).toInt()
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
    lend(player, itemId, itemIndex)
}

val decoder: ItemDecoder by inject()

fun offer(player: Player, id: Int, slot: Int, amount: Int) {
    if (!isValidAmount(player, amount)) {
        return
    }

    if (!canBeTraded(id)) {
        player.message("That item is not tradeable.")
        return
    }

    var amount = amount
    val currentAmount = player.inventory.getCount(id).toInt()
    if (amount > currentAmount) {
        amount = currentAmount
    }

    player.inventory.move(player.offer, id, amount, slot)
}

fun canBeTraded(id: Int): Boolean {
    val def = decoder.get(id)
    return def.notedTemplateId == -1 && def.lendTemplateId == -1 && def.singleNoteTemplateId == -1 && def.dummyItem == 0
}

fun lend(player: Player, id: Int, slot: Int) {
    if (!isValidAmount(player, 1)) {
        return
    }

    if (!canBeLent(player, id)) {
        return
    }

    player.inventory.move(player.loan, id, 1, slot)
}

fun canBeLent(player: Player, id: Int): Boolean {
    val itemDef = decoder.get(id)
    if (itemDef.lendId == -1) {
        val name = itemDef.name
        player.message("$name cannot be lent.")
        return false
    }
    return true
}