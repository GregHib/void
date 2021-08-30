import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on

on<EffectStart>({ effect == "freeze" }) { character: Character ->
    character.movement.frozen = true
}

on<EffectStop>({ effect == "freeze" }) { character: Character ->
    character.movement.frozen = false
}