package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.trade.Trade.isTrading
import world.gregs.voidps.world.interact.dialogue.type.intEntry

/**
 * Removing an item from an offer or loan
 */

on<InterfaceOption>({ id == "trade_main" && component == "offer_options" }) { player: Player ->
    val amount = when (option) {
        "Remove" -> 1
        "Remove-5" -> 5
        "Remove-10" -> 10
        "Remove-All" -> player.offer.getCount(item).toInt()
        else -> return@on
    }
    remove(player, item.id, itemSlot, amount)
}

on<InterfaceOption>({ id == "trade_main" && component == "offer_options" && option == "Remove-X" }) { player: Player ->
    player.dialogue {
        val amount = intEntry("Enter amount:")
        remove(player, item.id, itemSlot, amount)
    }
}

on<InterfaceOption>({ id == "trade_main" && component == "offer_options" && option == "Value" }) { player: Player ->
    player.message("${item.def.name} is priceless!", ChatType.Trade)
}

on<InterfaceOption>({ id == "trade_main" && component == "loan_item" && option == "Remove" }) { player: Player ->
    removeLend(player, item.id, 0)
}

fun remove(player: Player, id: String, slot: Int, amount: Int) {
    if (!isTrading(player, amount)) {
        return
    }
    var amount = amount
    val currentAmount = player.offer.getCount(id).toInt()
    if (amount > currentAmount) {
        amount = currentAmount
    }

    player.offer.move(player.inventory, id, amount, slot)
}

fun removeLend(player: Player, id: String, slot: Int) {
    if (!isTrading(player, 1)) {
        return
    }
    player.loan.move(player.inventory, id, 1, slot)
}