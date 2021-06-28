package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.colourOverlay
import world.gregs.voidps.engine.event.on

on<EffectStop>({ effect == "colour_overlay" }) { character: Character ->
    character.colourOverlay.apply {
        delay = 0
        duration = 0
        colour = 0
    }
}