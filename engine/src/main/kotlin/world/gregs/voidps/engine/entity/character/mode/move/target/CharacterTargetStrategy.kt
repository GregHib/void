package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.type.Tile

data class CharacterTargetStrategy(
    private val character: Character
) : TargetStrategy {
    override val bitMask = 0
    override val tile: Tile
        get() = character.tile
    override val width: Int
        get() = character.size
    override val height: Int
        get() = character.size
    override val rotation = 0
    override val exitStrategy = -2
    override val sizeX: Int
        get() = character.size
    override val sizeY: Int
        get() = character.size
}