import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on

on<EffectStart>({ effect == "freeze" || effect == "bind" || effect == "stun" }) { character: Character ->
    character.movement.frozen = true
    character.start("skilling_delay", ticks, quiet = true)
    if (effect == "stun") {
        character.start("stun_immunity", ticks + 1)
    }
    if (effect == "freeze" && character is Player) {
        character.message("You have been frozen!")
    }
}

on<EffectStop>({ effect == "freeze" || effect == "bind" || effect == "stun" }) { character: Character ->
    character.movement.frozen = false
    character.start("bind_immunity", 5)
}