package content.social.trade.lend

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.bank
import content.social.trade.loanReturnedItems
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit

object Loan {
    private val logger = InlineLogger()

    fun getSecondsRemaining(player: Player, timeKey: String): Int {
        val remaining = player.remaining(timeKey, epochSeconds())
        if (remaining == -1) {
            return 0
        }
        return remaining
    }

    private fun getMinutesRemaining(player: Player, timeKey: String): Int {
        val seconds = player.remaining(timeKey, epochSeconds())
        return TimeUnit.SECONDS.toMinutes(seconds.toLong()).toInt()
    }

    fun returnLoan(player: Player) {
        val item = player["borrowed_item", ""]
        if (item.isBlank()) {
            reset(player)
            logger.info { "Unable to find borrowed item for $player" }
            return
        }
        returnLoan(player, item)
    }

    fun returnLoan(player: Player, item: String) {
        if (!player.inventory.remove(item) && !player.equipment.remove(item) && !player.bank.remove(item) && !player.beastOfBurden.remove(item)) {
            logger.warn { "Player doesn't have lent item to remove $player $item" }
            return
        }
        reset(player)
        logger.info { "$player discarded item $item" }
        val name: String? = player["borrowed_from"]
        AuditLog.event(player, "returned", item, name)
        if (name == null) {
            logger.error { "Unable to find borrowed item partner for $player" }
            return
        }
        val lender = Players.get(name) ?: return
        lender.softTimers.stop("loan_message")
    }

    private fun reset(player: Player) {
        player.clear("borrowed_item")
        player.clear("borrow_timeout")
    }

    fun lendItem(borrower: Player, lender: Player, item: String, duration: Int) {
        val def = ItemDefinitions.get(item)
        val lend = ItemDefinitions.get(def.lendId).stringId
        if (!borrower.inventory.add(lend)) {
            logger.error { "Unable to add $lender's loan $item '$lend' for $duration to $borrower" }
            return
        }
        if (duration > 0) {
            val seconds = TimeUnit.HOURS.toSeconds(duration.toLong()).toInt()
            borrower.start("borrow_timeout", seconds, epochSeconds())
            lender.start("lend_timeout", seconds, epochSeconds())
        }
        borrower["borrowed_item"] = lend
        borrower["borrowed_from"] = lender.name
        borrower.softTimers.start("borrow_message")

        lender["lent_to"] = borrower.name
        lender["lent_item_id"] = lender.loanReturnedItems[0].def.id
        lender["lent_item_amount"] = lender.loanReturnedItems[0].amount
        lender.softTimers.start("loan_message")
    }

    fun getExpiry(player: Player, key: String): String {
        val seconds = getMinutesRemaining(player, key)
        val hours = seconds / 60
        val minutes = seconds.rem(60)
        val hour = if (hours > 0) " $hours ${"hour".plural(hours)}" else ""
        val minute = if (minutes > 0) " $minutes ${"minute".plural(minutes)}" else ""
        return "in$hour$minute"
    }
}
