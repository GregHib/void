package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ColourOverlay(
    var delay: Int = 0,
    var duration: Int = 0,
    var colour: Int = 0
) : Visual

fun Player.flagColourOverlay() = visuals.flag(0x20000)

fun NPC.flagColourOverlay() = visuals.flag(0x200)

fun Indexed.flagColourOverlay() {
    if (this is Player) flagColourOverlay() else if (this is NPC) flagColourOverlay()
}

fun Indexed.getColourOverlay() = visuals.getOrPut(ColourOverlay::class) { ColourOverlay() }

fun Indexed.setColourOverlay(delay: Int = 0, duration: Int = 0, colour: Int = 0) {
    val overlay = getColourOverlay()
    overlay.delay = delay
    overlay.duration = duration
    overlay.colour = colour
    flagColourOverlay()
}