package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Indexed
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

fun Player.flagTimeBar() = visuals.flag(0x2000)

fun NPC.flagTimeBar() = visuals.flag(0x100)

fun Indexed.flagTimeBar() {
    if (this is Player) flagTimeBar() else if (this is NPC) flagTimeBar()
}

fun Indexed.getTimeBar() = visuals.getOrPut(TimeBar::class) { TimeBar() }

fun Indexed.setTimeBar(full: Boolean = false, exponentialDelay: Int = 0, delay: Int = 0, increment: Int = 0) {
    val bar = getTimeBar()
    bar.full = full
    bar.exponentialDelay = exponentialDelay
    bar.delay = delay
    bar.increment = increment
    flagTimeBar()
}
