package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.beastOfBurden
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.activity.bank.bank
import java.util.concurrent.TimeUnit

object Loan {
    private val definitions: ItemDefinitions by inject()
    private val logger = InlineLogger()

    fun startLendTimer(player: Player) {
        if (!player.contains("lent_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "lend_timeout")
        if (remaining < 0) {
            player.message("The item you lent has been returned to your collection box.")
        } else if (remaining > 0) {
            val ticks = TimeUnit.MINUTES.toTicks(remaining + 1L)
            delay(player, ticks) {
                player.message("The item you lent has been returned to your collection box.")
            }
        }
    }

    fun startBorrowTimer(player: Player) {
        if (!player.contains("borrowed_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "borrow_timeout")
        if (remaining < 0) {
            player.message("The item you borrowed has been returned to its owner.")
            returnLoan(player)
        } else if (remaining > 0) {
            val ticks = TimeUnit.MINUTES.toTicks(remaining.toLong())
            delay(player, ticks) {
                player.message("The item you borrowed will be returned to its owner in a minute.")
                delay(player, TimeUnit.MINUTES.toTicks(1)) {
                    player.message("Your loan has expired; the item you borrowed will now be returned to its owner.")
                    returnLoan(player)
                }
            }
        }
    }

    fun getTimeRemaining(player: Player, timeKey: String): Long {
        return when {
            player.contains(timeKey) -> {
                val timeout: Int = player.getOrNull(timeKey) ?: return -1
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
        player.clear("borrowed_item")
        player.clear("borrow_timeout")
    }

    fun lendItem(player: Player, other: Player, item: String, duration: Int) {
        val def = definitions.get(item)
        val lend = definitions.getName(def.lendId)
        if (player.inventory.add(lend)) {
            if (duration > 0) {
                val millis = TimeUnit.HOURS.toMillis(duration.toLong()) - TimeUnit.MINUTES.toMillis(1L)
                player["borrow_timeout", true] = System.currentTimeMillis() + millis
                other["lend_timeout", true] = System.currentTimeMillis() + millis
            }
            player["borrowed_item", true] = lend
            other["lent_item", true] = item
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