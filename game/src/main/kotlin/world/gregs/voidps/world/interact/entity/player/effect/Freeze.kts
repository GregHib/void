import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on

on<EffectStart>({ effect == "freeze" || effect == "bind" || effect == "stun" }) { character: Character ->
    // "frozen" is the underlying movement blocker, for when we don't want to send a message.
    character.start("frozen", ticks)
    character.start("skilling_delay", ticks, quiet = true)
    if (effect == "stun") {
        character.start("stun_immunity", ticks + 1)
    }
    if (effect == "freeze" && character is Player) {
        character.message("You have been frozen!")
    }
}

on<EffectStop>({ effect == "freeze" || effect == "bind" || effect == "stun" }) { character: Character ->
    character.stop("frozen")
    character.start("bind_immunity", 5)
}