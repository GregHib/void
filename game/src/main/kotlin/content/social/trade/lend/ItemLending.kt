package content.social.trade.lend

import content.social.trade.lend.Loan.returnLoan
import content.social.trade.loanReturnedItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

/**
 * Reschedule timers on player login
 * On logout return items borrowed or lent until logout
 */

class ItemLending(val players: Players) : Script {

    init {
        playerSpawn {
            checkBorrowComplete(this)
            checkLoanComplete(this)
        }

        playerDespawn {
            checkBorrowUntilLogout(this)
            checkLoanUntilLogout(this)
        }

        timerStart("borrow_message") { TimeUnit.MINUTES.toTicks(1) }

        timerStart("loan_message") {
            val remaining = remaining("lend_timeout", epochSeconds())
            if (remaining == -1) 0 else TimeUnit.SECONDS.toTicks(remaining)
        }

        timerTick("borrow_message", ::checkExpiry)

        timerStop("loan_message") { logout ->
            if (!logout) {
                stopLending(this)
            }
        }

        timerStop("borrow_message") { logout ->
            if (!logout) {
                returnLoan(this)
            }
        }
    }

    fun checkExpiry(player: Player): Int {
        val remaining = player.remaining("borrow_timeout", epochSeconds())
        if (remaining <= 0) {
            player.message("Your loan has expired; the item you borrowed will now be returned to its owner.")
            return Timer.CANCEL
        } else if (remaining == 60) {
            player.message("The item you borrowed will be returned to its owner in a minute.")
        }
        return Timer.CONTINUE
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
        if (!player.loanReturnedItems.isFull()) {
            return
        }
        val remaining = player.remaining("lend_timeout", epochSeconds())
        if (remaining <= 0) {
            stopLending(player)
        } else {
            player.softTimers.start("loan_message", true)
        }
    }

    fun checkBorrowUntilLogout(player: Player) {
        if (!player.contains("borrow_timeout") && player.contains("borrowed_item")) {
            returnLoan(player)
        }
    }

    fun checkLoanUntilLogout(player: Player) {
        if (!player.contains("lend_timeout") && player.loanReturnedItems.isFull() && player.contains("lent_to")) {
            val name: String? = player["lent_to"]
            player.stop("lend_timeout")
            player.softTimers.stop("loan_message")
            val borrower = players.get(name ?: return) ?: return
            borrower.stop("borrow_timeout")
            borrower.softTimers.stop("borrow_message")
            borrower.message("The item you borrowed has been returned to its owner.")
        }
    }

    fun stopLending(player: Player) {
        player.message("The item you lent has been returned to your collection box.")
        player.clear("lent_to")
        player.clear("lent_item_id")
        player.clear("lent_item_amount")
    }
}
