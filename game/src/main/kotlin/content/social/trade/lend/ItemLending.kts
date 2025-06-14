package content.social.trade.lend

import content.social.trade.lend.Loan.returnLoan
import content.social.trade.returnedItems
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

/**
 * Reschedule timers on player login
 * On logout return items borrowed or lent until logout
 */
val players: Players by inject()

playerSpawn { player ->
    checkBorrowComplete(player)
    checkLoanComplete(player)
}

fun checkBorrowComplete(player: Player) {
    if (!player.contains("borrowed_item")) {
        return
    }
    val remaining = player.remaining("borrow_timeout", epochSeconds())
    if (remaining <= 0) {
        player.message("The item you borrowed has been returned to its owner.")
        returnLoan(player)
    } else {
        player.softTimers.start("borrow_message", true)
    }
}

fun checkLoanComplete(player: Player) {
    if (!player.returnedItems.isFull()) {
        return
    }
    val remaining = player.remaining("lend_timeout", epochSeconds())
    if (remaining <= 0) {
        stopLending(player)
    } else {
        player.softTimers.start("loan_message", true)
    }
}

playerDespawn { player ->
    checkBorrowUntilLogout(player)
    checkLoanUntilLogout(player)
}

fun checkBorrowUntilLogout(player: Player) {
    if (!player.contains("borrow_timeout") && player.contains("borrowed_item")) {
        returnLoan(player)
    }
}

fun checkLoanUntilLogout(player: Player) {
    if (!player.contains("lend_timeout") && player.returnedItems.isFull() && player.contains("lent_to")) {
        val name: String? = player["lent_to"]
        player.stop("lend_timeout")
        player.softTimers.stop("loan_message")
        val borrower = players.get(name ?: return) ?: return
        borrower.stop("borrow_timeout")
        borrower.softTimers.stop("borrow_message")
        borrower.message("The item you borrowed has been returned to its owner.")
    }
}

timerStart("loan_message") { player ->
    val remaining = player.remaining("lend_timeout", epochSeconds())
    interval = TimeUnit.SECONDS.toTicks(remaining)
}

timerStop("loan_message") { player ->
    if (!logout) {
        stopLending(player)
    }
}

timerStart("borrow_message") {
    interval = TimeUnit.MINUTES.toTicks(1)
}

timerTick("borrow_message") { player ->
    val remaining = player.remaining("borrow_timeout", epochSeconds())
    if (remaining <= 0) {
        player.message("Your loan has expired; the item you borrowed will now be returned to its owner.")
        cancel()
    } else if (remaining == 60) {
        player.message("The item you borrowed will be returned to its owner in a minute.")
    }
}

timerStop("borrow_message") { player ->
    if (!logout) {
        returnLoan(player)
    }
}

fun stopLending(player: Player) {
    player.message("The item you lent has been returned to your collection box.")
    player.clear("lent_to")
    player.clear("lent_item_id")
    player.clear("lent_item_amount")
}
