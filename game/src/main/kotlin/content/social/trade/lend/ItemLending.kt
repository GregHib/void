package content.social.trade.lend

import content.social.trade.lend.Loan.returnLoan
import content.social.trade.returnedItems
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

@Script
class ItemLending : Api {

    val players: Players by inject()

    override fun spawn(player: Player) {
        checkBorrowComplete(player)
        checkLoanComplete(player)
    }

    @Timer("loan_message,borrow_message")
    override fun start(player: Player, timer: String, restart: Boolean): Int {
        if (timer == "borrow_message") {
            return TimeUnit.MINUTES.toTicks(1)
        }
        val remaining = player.remaining("lend_timeout", epochSeconds())
        return TimeUnit.SECONDS.toTicks(remaining)
    }

    @Timer("borrow_message")
    override fun tick(player: Player, timer: String): Int {
        val remaining = player.remaining("borrow_timeout", epochSeconds())
        if (remaining <= 0) {
            player.message("Your loan has expired; the item you borrowed will now be returned to its owner.")
            return Timer.CANCEL
        } else if (remaining == 60) {
            player.message("The item you borrowed will be returned to its owner in a minute.")
        }
        return Timer.CONTINUE
    }

    @Timer("loan_message,borrow_message")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        if (!logout) {
            if (timer == "loan_message") {
                stopLending(player)
            } else {
                returnLoan(player)
            }
        }
        super.stop(player, timer, logout)
    }

    init {
        playerDespawn { player ->
            checkBorrowUntilLogout(player)
            checkLoanUntilLogout(player)
        }
    }

    /**
     * Reschedule timers on player login
     * On logout return items borrowed or lent until logout
     */

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

    fun stopLending(player: Player) {
        player.message("The item you lent has been returned to your collection box.")
        player.clear("lent_to")
        player.clear("lent_item_id")
        player.clear("lent_item_amount")
    }
}
