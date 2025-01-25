package content.entity.effect

import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.engine.timer.characterTimerTick

characterTimerStart("colour_overlay") { character ->
    val overlay = character.visuals.colourOverlay
    interval = (overlay.delay + overlay.duration) / 30
}

characterTimerTick("colour_overlay") {
    cancel()
}

characterTimerStop("colour_overlay") { character ->
    character.visuals.colourOverlay.reset()
}