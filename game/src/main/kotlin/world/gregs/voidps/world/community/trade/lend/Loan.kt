package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.contain.*
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.activity.bank.bank
import java.util.concurrent.TimeUnit

object Loan {
    private val definitions: ItemDefinitions by inject()
    private val logger = InlineLogger()

    fun startLendTimer(player: Player, restart: Boolean) {
        if (!player.contains("lent_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "lend_timeout")
        if (remaining < 0) {
            player.message("The item you lent has been returned to your collection box.")
        } else if (remaining > 0) {
            player.softTimers.start("loan_message", restart)
        }
    }

    fun startBorrowTimer(player: Player, restart: Boolean) {
        if (!player.contains("borrowed_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "borrow_timeout")
        if (remaining < 0) {
            player.message("The item you borrowed has been returned to its owner.")
            returnLoan(player)
        } else if (remaining > 0) {
            player.softTimers.start("borrow_message", restart)
        }
    }

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
        reset(player)
        if (!player.inventory.remove(item) && !player.equipment.remove(item) && !player.bank.remove(item) && !player.beastOfBurden.remove(item)) {
            logger.warn { "Player doesn't have lent item to remove $player $item" }
            return
        }
        logger.info { "$player discarded item $item" }
        val name: String? = player.getOrNull("borrowed_from")
        if (name == null) {
            logger.error { "Unable to find borrowed item partner for $player" }
        } else {
            player.softTimers.stop("lend_timeout")
            val partner = get<Players>().get(name) ?: return
            partner.softTimers.stop("lend_timeout")
            partner.message("The item you lent has been returned to your collection box.")
        }
    }

    private fun reset(player: Player) {
        player.clear("borrowed_item")
        player.clear("borrow_timeout")
    }

    fun lendItem(player: Player, other: Player, item: String, duration: Int) {
        val def = definitions.get(item)
        val lend = definitions.get(def.lendId).stringId
        if (player.inventory.add(lend)) {
            if (duration > 0) {
                val seconds = TimeUnit.HOURS.toSeconds(duration.toLong()).toInt()
                player.start("borrow_timeout", seconds, epochSeconds())
                other.start("lend_timeout", seconds, epochSeconds())
            }
            player["borrowed_item"] = lend
            player["borrowed_from"] = other.name
            other["lent_item"] = item
            other["lent_to"] = player.name
            startBorrowTimer(player, false)
            startLendTimer(other, false)
        }
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