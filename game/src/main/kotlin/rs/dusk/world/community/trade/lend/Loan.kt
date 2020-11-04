package rs.dusk.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.entity.character.*
import rs.dusk.engine.entity.character.contain.beastOfBurden
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.delay
import rs.dusk.utility.Time
import rs.dusk.utility.func.plural
import rs.dusk.utility.inject
import rs.dusk.world.activity.bank.bank
import java.util.concurrent.TimeUnit

object Loan {
    private val definitions: ItemDefinitions by inject()
    private val executor: TaskExecutor by inject()
    private val logger = InlineLogger()

    fun startLendTimer(player: Player) {
        if (!player.has("lent_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "lend_timeout")
        if (remaining < 0) {
            player.message("The item you lent has been returned to your collection box.")
        } else if (remaining > 0) {
            val ticks = Time.minutesToTicks(remaining + 1)
            executor.delay(player, ticks) {
                player.message("The item you lent has been returned to your collection box.")
            }
        }
    }

    fun startBorrowTimer(player: Player) {
        if (!player.has("borrowed_item")) {
            return
        }
        val remaining = getMinutesRemaining(player, "borrow_timeout")
        if (remaining < 0) {
            player.message("The item you borrowed has been returned to its owner.")
            returnLoan(player)
        } else if (remaining > 0) {
            val ticks = Time.minutesToTicks(remaining)
            executor.delay(player, ticks) {
                player.message("The item you borrowed will be returned to its owner in a minute.")
                executor.delay(player, Time.minutesToTicks(1)) {
                    player.message("Your loan has expired; the item you borrowed will now be returned to its owner.")
                    returnLoan(player)
                }
            }
        }
    }

    fun getTimeRemaining(player: Player, timeKey: String): Long {
        return when {
            player.has(timeKey) -> {
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
        val item = player["borrowed_item", -1]
        if (item == -1) {
            reset(player)
            logger.info { "Unable to find borrowed item for $player" }
            return
        }
        returnLoan(player, item)
    }

    fun returnLoan(player: Player, item: Int) {
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

    fun lendItem(player: Player, other: Player, item: Int, duration: Int) {
        val def = definitions.get(item)
        if (player.inventory.add(def.lendId)) {
            if (duration > 0) {
                val millis = TimeUnit.HOURS.toMillis(duration.toLong()) - TimeUnit.MINUTES.toMillis(1L)
                player["borrow_timeout", true] = System.currentTimeMillis() + millis
                other["lend_timeout", true] = System.currentTimeMillis() + millis
            }
            player["borrowed_item", true] = def.lendId
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