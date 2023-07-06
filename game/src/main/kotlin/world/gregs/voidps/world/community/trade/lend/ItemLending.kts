package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.community.trade.lend.Loan.returnLoan
import world.gregs.voidps.world.community.trade.lend.Loan.startBorrowTimer
import world.gregs.voidps.world.community.trade.lend.Loan.startLendTimer
import java.util.concurrent.TimeUnit

/**
 * Reschedule timers on player login
 * On logout return items borrowed or lent until logout
 */

val logger = InlineLogger()
val players: Players by inject()

on<Registered> { player: Player ->
    startLendTimer(player, true)
    startBorrowTimer(player, true)
    if (player.contains("expired_message")) {
        player.softTimers.restart("expired_message")
    }
}

on<Unregistered> { player: Player ->
    if (!player.contains("borrow_timeout") && player.contains("borrowed_item")) {
        returnLoan(player)
    }
    if (!player.contains("lend_timeout") && player.contains("lent_item")) {
        val name: String? = player.getOrNull("lent_to")
        if (name == null) {
            logger.error { "Unable to find lent item partner for $player" }
            return@on
        }
        player.stop("lend_timeout")
        val partner = players.get(name) ?: return@on
        partner.stop("lend_timeout")
        partner.message("The item you borrowed has been returned to its owner.")
    }
}

on<TimerStart>({ timer == "loan_message" }) { player: Player ->
    val remaining = player.remaining("lend_timeout", epochSeconds())
    interval = TimeUnit.SECONDS.toTicks(remaining + 1)
}

on<TimerStop>({ timer == "loan_message" }) { player: Player ->
    player.message("The item you lent has been returned to your collection box.")
}

on<TimerStart>({ timer == "borrow_message" }) { player: Player ->
    val remaining = player.remaining("borrow_timeout", epochSeconds())
    interval = TimeUnit.SECONDS.toTicks(remaining)
}

on<TimerStop>({ timer == "borrow_message" }) { player: Player ->
    player.message("The item you borrowed will be returned to its owner in a minute.")
    player.softTimers.start("expired_message")
}

on<TimerStart>({ timer == "expired_message" }) { _: Player ->
    interval = TimeUnit.MINUTES.toTicks(1)
}

on<TimerStop>({ timer == "expired_message" }) { player: Player ->
    player.message("Your loan has expired; the item you borrowed will now be returned to its owner.")
    returnLoan(player)
}