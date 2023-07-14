package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.player.Player

interface TargetPlayerContext : CharacterContext {
    val target: Player
}