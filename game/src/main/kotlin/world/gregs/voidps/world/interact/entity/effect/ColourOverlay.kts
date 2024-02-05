package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.engine.timer.characterTimerTick

characterTimerStart({ timer == "colour_overlay" }) { character: Character ->
    val overlay = character.visuals.colourOverlay
    interval = (overlay.delay + overlay.duration) / 30
}

characterTimerTick({ timer == "colour_overlay" }) { _: Character ->
    cancel()
}

characterTimerStop({ timer == "colour_overlay" }) { character: Character ->
    character.visuals.colourOverlay.reset()
}