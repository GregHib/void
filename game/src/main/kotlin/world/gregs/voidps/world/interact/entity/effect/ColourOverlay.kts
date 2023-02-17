package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.stopSoftTimer

on<TimerTick>({ timer == "colour_overlay" }) { character: Character ->
    character.visuals.colourOverlay.reset()
    character.stopSoftTimer(timer)
}