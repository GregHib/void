package world.gregs.voidps.world.community.trade

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.req.request
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.MoveItem.moveAll
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.lend.Loan

/**
 * Both players accepting the request moves onto the confirmation screen.
 * Both players accepting the confirmation exchanges items and finishes the trade.
 */

val logger = InlineLogger()

interfaceOption("Accept", "accept", "trade_main") {
    val partner = getPartner(player) ?: return@interfaceOption
    if (player.offer.count + player.loan.count > partner.inventory.spaces) {
        player.message("Other player doesn't have enough inventory space to accept this trade.")
        return@interfaceOption
    }
    if (partner.offer.count + partner.loan.count > player.inventory.spaces) {
        player.message("You don't have enough inventory space to accept this trade.")
        return@interfaceOption
    }
    player.interfaces.sendText("trade_main", "status", "Waiting for other player...")
    partner.interfaces.sendText("trade_main", "status", "Other player has accepted.")
    player.request(partner, "accept_trade") { requester, acceptor ->
        confirm(requester)
        confirm(acceptor)
    }
}

fun confirm(player: Player) {
    player.interfaces.apply {
        remove("trade_main")
        open("trade_confirm")
        sendItemsList(player, "give", player.offer)
        sendItemsList(player, "receive", player.otherOffer)
        sendLoan(player)
    }
    player.interfaces.sendText("trade_confirm", "status", "Are you sure you want to make this trade?")
}

interfaceOption("Accept", "accept", "trade_confirm") {
    val partner = getPartner(player) ?: return@interfaceOption
    player.interfaces.sendText("trade_confirm", "status", "Waiting for other player...")
    partner.interfaces.sendText("trade_confirm", "status", "Other player has accepted.")
    player.request(partner, "confirm_trade") { requester, acceptor ->
        val requesterLoan = requester.loan[0]
        val acceptorLoan = acceptor.loan[0]
        val success = acceptor.offer.transaction {
            moveAll(requester.inventory)
            link(requester.offer).moveAll(acceptor.inventory)
            link(requester.loan).moveAll(requester.returnedItems)
            link(acceptor.loan).moveAll(acceptor.returnedItems)
        }
        if (!success) {
            logger.info { "Issue exchanging items $player ${player.offer} ${player.otherOffer} ${player.loan} ${player.otherLoan} ${player.inventory}" }
            requester.closeMenu()
            return@request
        }
        acceptor.message("Accepted trade.", ChatType.Trade)
        requester.message("Accepted trade.", ChatType.Trade)
        loanItem(requester, acceptorLoan, acceptor)
        loanItem(acceptor, requesterLoan, requester)
        requester.closeMenu()
    }
}

fun loanItem(player: Player, loanItem: Item, other: Player) {
    val duration = other["lend_time", -1]
    if (loanItem.id.isBlank() || duration == -1) {
        return
    }
    Loan.lendItem(player, other, loanItem.id, duration)
}

fun sendLoan(player: Player) {
    sendLoan(player, player.loan, "lend_time", "lend")
    sendLoan(player, player.otherLoan, "other_lend_time", "other_lend")
}

fun sendLoan(player: Player, inventory: Inventory, key: String, prefix: String) {
    val lend = inventory.isFull()
    player.interfaces.sendVisibility("trade_confirm", "${prefix}_container", lend)
    if (lend) {
        val time = if (player[key, -1] <= 0) "until logout" else "${player[key, -1]} hours"
        val description = "Lend: ${Colours.WHITE.toTag()} ${inventory[0].def.name}, $time"
        player.interfaces.sendText("trade_confirm", "${prefix}_text", description)
    }
}

fun sendItemsList(player: Player, prefix: String, inventory: Inventory) {
    val middle = inventory.count <= 14
    player.interfaces.sendVisibility("trade_confirm", "${prefix}_middle", middle)
    player.interfaces.sendVisibility("trade_confirm", "${prefix}_right", !middle)
    player.interfaces.sendVisibility("trade_confirm", "${prefix}_left", !middle)
    if (middle) {
        player.interfaces.sendText("trade_confirm", "${prefix}_middle", itemsList(inventory.items.toList(), true))
    } else {
        player.interfaces.sendText("trade_confirm", "${prefix}_left", itemsList(inventory.items.take(14), false))
        player.interfaces.sendText("trade_confirm", "${prefix}_right", itemsList(inventory.items.drop(14), false))
    }
}

fun itemsList(items: List<Item>, exact: Boolean) = buildString {
    for (item in items) {
        if (item.isEmpty()) {
            continue
        }
        append(Colours.ORANGE.toTag())
        append(item.def.name)
        if (item.amount > 1) {
            append(Colours.WHITE.toTag())
            append(" x ")
            append(item.amount.toPrefix())
            if (exact && item.amount > 10_000) {
                append(" (").append(item.amount.toLong().toDigitGroupString()).append(")")
            }
        }
        append("<br>")
    }
}

fun Int.toPrefix(): String {
    return when {
        this >= 10_000_000 -> "${(this / 1_000_000).toDigitGroupString()}M"
        this >= 10_000 -> "${(this / 1_000).toDigitGroupString()}K"
        else -> toString()
    }
}
