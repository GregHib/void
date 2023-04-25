package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.containsVarbit
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.trade.lend.Loan.returnLoan
import world.gregs.voidps.world.community.trade.lend.Loan.startBorrowTimer
import world.gregs.voidps.world.community.trade.lend.Loan.startLendTimer

/**
 * Reschedule timers on player login
 * On logout return items borrowed or lent until logout
 */

val logger = InlineLogger()

on<Registered> { player: Player ->
    startLendTimer(player)
    startBorrowTimer(player)
}

on<Unregistered> { player: Player ->
    if (!player.contains("borrow_timeout") && player.contains("borrowed_item")) {
        returnLoan(player)
        val partner: Player? = player.getOrNull("borrowed_from")
        if (partner == null) {
            logger.error { "Unable to find borrowed item partner for $player" }
        } else {
            reset(player, partner)
            partner.message("The item you lent has been returned to your collection box.")
        }
    }
    if (!player.contains("lend_timeout") && player.contains("lent_item")) {
        val partner: Player? = player.getOrNull("lent_to")
        if (partner == null) {
            logger.error { "Unable to find lent item partner for $player" }
            return@on
        }
        reset(partner, player)
        partner.message("The item you borrowed has been returned to its owner.")
    }
}

fun reset(borrower: Player, lender: Player) {
    val time = System.currentTimeMillis() - 1
    lender["lend_timeout"] = time
    borrower["borrow_timeout"] = time
}