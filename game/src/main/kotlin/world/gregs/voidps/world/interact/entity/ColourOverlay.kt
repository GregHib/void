package world.gregs.voidps.world.interact.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.colourOverlay
import world.gregs.voidps.engine.entity.character.update.visual.flagColourOverlay
import world.gregs.voidps.engine.entity.start

fun Character.colourOverlay(colour: Int, delay: Int, duration: Int) {
    colourOverlay.colour = colour
    colourOverlay.delay = delay
    colourOverlay.duration = duration
    flagColourOverlay()
    start("colour_overlay", (delay + duration) / 30)
}