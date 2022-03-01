package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on

on<EffectStop>({ effect == "colour_overlay" }) { character: Character ->
    character.visuals.colourOverlay.reset(character)
}