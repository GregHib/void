package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext

interface PlayerContext : CharacterContext {
    override val player: Player
    override val character: Character
        get() = player
}