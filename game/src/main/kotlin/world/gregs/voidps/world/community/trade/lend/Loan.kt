package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.contain.*
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.activity.bank.bank
import java.util.concurrent.TimeUnit

object Loan {
    private val definitions: ItemDefinitions by inject()
    private val logger = InlineLogger()

    fun startLendTimer(player: Player) {
        if (!player.hasVar("lent_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "lend_timeout")
        if (remaining < 0) {
            player.message("The item you lent has been returned to your collection box.")
        } else if (remaining > 0) {
            val ticks = TimeUnit.MINUTES.toTicks(remaining + 1)
            player.softQueue("loan_message", ticks) {
                player.message("The item you lent has been returned to your collection box.")
            }
        }
    }

    fun startBorrowTimer(player: Player) {
        if (!player.hasVar("borrowed_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "borrow_timeout")
        if (remaining < 0) {
            player.message("The item you borrowed has been returned to its owner.")
            returnLoan(player)
        } else if (remaining > 0) {
            val ticks = TimeUnit.MINUTES.toTicks(remaining)
            player.softQueue("borrow_message", ticks) {
                player.message("The item you borrowed will be returned to its owner in a minute.")
                player.softQueue("expired_message", TimeUnit.MINUTES.toTicks(1)) {
                    player.message("Your loan has expired; the item you borrowed will now be returned to its owner.")
                    returnLoan(player)
                }
            }
        }
    }

    fun getTimeRemaining(player: Player, timeKey: String): Long {
        return when {
            player.hasVar(timeKey) -> {
                val timeout: Long = player.getOrNull(timeKey) ?: return -1
                System.currentTimeMillis() - timeout
            }
            else -> 0
        }
    }

    fun getMinutesRemaining(player: Player, timeKey: String): Int {
        val millis = getTimeRemaining(player, timeKey)
        return TimeUnit.MILLISECONDS.toMinutes(millis).toInt()
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
        reset(player)
        if (!player.inventory.remove(item) && !player.equipment.remove(item) && !player.bank.remove(item) && !player.beastOfBurden.remove(item)) {
            logger.warn { "Player doesn't have lent item to remove $player $item" }
            return
        }
    }

    private fun reset(player: Player) {
        player.clearVar("borrowed_item")
        player.clearVar("borrow_timeout")
    }

    fun lendItem(player: Player, other: Player, item: String, duration: Int) {
        val def = definitions.get(item)
        val lend = definitions.get(def.lendId).stringId
        if (player.inventory.add(lend)) {
            if (duration > 0) {
                val millis = TimeUnit.HOURS.toMillis(duration.toLong()) - TimeUnit.MINUTES.toMillis(1L)
                player["borrow_timeout"] = System.currentTimeMillis() + millis
                other["lend_timeout"] = System.currentTimeMillis() + millis
            }
            player["borrowed_item"] = lend
            other["lent_item"] = item
            player["borrowed_from"] = other
            other["lent_to"] = player
            startBorrowTimer(player)
        }
    }

    fun getExpiry(player: Player, key: String): String {
        val remainder = getMinutesRemaining(player, key)
        val hours = remainder / 60
        val minutes = remainder.rem(60)
        val hour = if (hours > 0) " $hours ${"hour".plural(hours)}" else ""
        val minute = if (minutes > 0) " $minutes ${"minute".plural(minutes)}" else ""
        return "in$hour$minute"
    }
}