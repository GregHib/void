package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.getOrNull
import world.gregs.voidps.engine.entity.character.has
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRegistered
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.priority
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.world.community.trade.lend.Loan.returnLoan
import world.gregs.voidps.world.community.trade.lend.Loan.startBorrowTimer
import world.gregs.voidps.world.community.trade.lend.Loan.startLendTimer

/**
 * Reschedule timers on player login
 * On logout return items borrowed or lent until logout
 */

val logger = InlineLogger()

PlayerRegistered then {
    startLendTimer(player)
    startBorrowTimer(player)
}

Unregistered priority Priority.HIGH where { entity is Player } then {
    val player = entity as Player
    if (!player.has("borrow_timeout") && player.has("borrowed_item")) {
        returnLoan(player)
        val partner: Player? = player.getOrNull("borrowed_from")
        if (partner == null) {
            logger.error { "Unable to find borrowed item partner for $player" }
        } else {
            reset(player, partner)
            partner.message("The item you lent has been returned to your collection box.")
        }
    }
    if (!player.has("lend_timeout") && player.has("lent_item")) {
        val partner: Player? = player.getOrNull("lent_to")
        if (partner == null) {
            logger.error { "Unable to find lent item partner for $player" }
            return@then
        }
        reset(partner, player)
        partner.message("The item you borrowed has been returned to its owner.")
    }
}

fun reset(borrower: Player, lender: Player) {
    val time = System.currentTimeMillis() - 1
    lender["lend_timeout", true] = time
    borrower["borrow_timeout", true] = time
}