package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<EffectStart>({ effect == "teleport_block" }) { character: Character ->
    if (character is Player) {
        character.message("You have been teleblocked.")
    }
}