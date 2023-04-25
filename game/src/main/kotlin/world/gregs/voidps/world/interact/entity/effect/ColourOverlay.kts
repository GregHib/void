package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick

on<TimerStart>({ timer == "colour_overlay" }) { character: Character ->
    val overlay = character.visuals.colourOverlay
    interval = (overlay.delay + overlay.duration) / 30
}

on<TimerTick>({ timer == "colour_overlay" }) { _: Character ->
    cancel()
}

on<TimerStop>({ timer == "colour_overlay" }) { character: Character ->
    character.visuals.colourOverlay.reset()
}