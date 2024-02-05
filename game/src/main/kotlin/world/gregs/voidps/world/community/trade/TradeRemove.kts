package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.community.trade.Trade.isTrading
import world.gregs.voidps.world.interact.dialogue.type.intEntry

/**
 * Removing an item from an offer or loan
 */

interfaceOption("trade_main", "offer_options") {
    val amount = when (option) {
        "Remove" -> 1
        "Remove-5" -> 5
        "Remove-10" -> 10
        "Remove-All" -> player.offer.count(item.id)
        "Remove-X" -> intEntry("Enter amount:")
        else -> return@interfaceOption
    }
    remove(player, item.id, itemSlot, amount)
}

interfaceOption("trade_main", "offer_options", "Value") {
    player.message("${item.def.name} is priceless!", ChatType.Trade)
}

interfaceOption("trade_main", "loan_item", "Remove") {
    removeLend(player, item.id, 0)
}

fun remove(player: Player, id: String, slot: Int, amount: Int) {
    if (!isTrading(player, amount)) {
        return
    }
    player.offer.transaction {
        val added = link(player.inventory).addToLimit(id, amount)
        if (!inventory.stackable(id) && added == 1) {
            clear(slot)
        } else {
            removeToLimit(id, added)
        }
    }
}

fun removeLend(player: Player, id: String, slot: Int) {
    if (!isTrading(player, 1)) {
        return
    }
    player.loan.transaction {
        clear(slot)
        link(player.inventory).add(id, 1)
    }
}