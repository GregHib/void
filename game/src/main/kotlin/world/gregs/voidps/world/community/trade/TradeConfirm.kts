package world.gregs.voidps.world.community.trade

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeInterface
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.req.request
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.community.trade.lend.Loan

/**
 * Both players accepting the request moves onto the confirmation screen.
 * Both players accepting the confirmation exchanges items and finishes the trade.
 */

val logger = InlineLogger()

on<InterfaceOption>({ id == "trade_main" && component == "accept" && option == "Accept" }) { player: Player ->
    val partner = getPartner(player) ?: return@on
    if (player.offer.count - partner.offer.count > partner.inventory.spaces) {
        player.message("Other player doesn't have enough inventory space to accept this trade.")
        return@on
    }
    if (partner.offer.count - player.offer.count > player.inventory.spaces) {
        player.message("You don't have enough inventory space to accept this trade.")
        return@on
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
    }
    player.interfaces.sendText("trade_confirm", "status", "Are you sure you want to make this trade?")
}

on<InterfaceOption>({ id == "trade_confirm" && component == "accept" && option == "Accept" }) { player: Player ->
    val partner = getPartner(player) ?: return@on
    player.interfaces.sendText("trade_confirm", "status", "Waiting for other player...")
    partner.interfaces.sendText("trade_confirm", "status", "Other player has accepted.")
    player.request(partner, "confirm_trade") { requester, acceptor ->
        val success = acceptor.offer.transaction {
            moveAll(requester.inventory)
            link(requester.offer).moveAll(acceptor.inventory)
        }
        if (!success) {
            logger.info { "Issue exchanging items $player ${player.offer} ${player.otherOffer} ${player.loan} ${player.otherLoan} ${player.inventory}" }
            requester.closeInterface()
            return@request
        }
        loanItem(requester, acceptor)
        loanItem(acceptor, requester)
        requester.closeInterface()
    }
}

fun loanItem(player: Player, other: Player) {
    val loanItem = player.otherLoan[0].id
    val duration = other.getVar("lend_time", -1)
    if (loanItem.isBlank() || duration == -1) {
        return
    }
    Loan.lendItem(player, other, loanItem, duration)
}