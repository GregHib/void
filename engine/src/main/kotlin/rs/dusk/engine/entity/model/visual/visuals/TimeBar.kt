package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class TimeBar(
    var full: Boolean = false,
    var exponentialDelay: Int = 0,
    var delay: Int = 0,
    var increment: Int = 0
) : Visual

const val PLAYER_TIME_BAR_MASK = 0x2000

const val NPC_TIME_BAR_MASK = 0x100

fun Player.flagTimeBar() = visuals.flag(PLAYER_TIME_BAR_MASK)

fun NPC.flagTimeBar() = visuals.flag(NPC_TIME_BAR_MASK)

fun Player.getTimeBar() = visuals.getOrPut(PLAYER_TIME_BAR_MASK) { TimeBar() }

fun NPC.getTimeBar() = visuals.getOrPut(NPC_TIME_BAR_MASK) { TimeBar() }

fun Player.setTimeBar(full: Boolean = false, exponentialDelay: Int = 0, delay: Int = 0, increment: Int = 0) {
    setTimeBar(getTimeBar(), full, exponentialDelay, delay, increment)
    flagTimeBar()
}

fun NPC.setTimeBar(full: Boolean = false, exponentialDelay: Int = 0, delay: Int = 0, increment: Int = 0) {
    setTimeBar(getTimeBar(), full, exponentialDelay, delay, increment)
    flagTimeBar()
}

private fun setTimeBar(bar: TimeBar, full: Boolean, exponentialDelay: Int, delay: Int, increment: Int) {
    bar.full = full
    bar.exponentialDelay = exponentialDelay
    bar.delay = delay
    bar.increment = increment
}
