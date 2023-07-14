package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.player.Player

interface PlayerTargetContext : CharacterContext {
    val target: Player
}