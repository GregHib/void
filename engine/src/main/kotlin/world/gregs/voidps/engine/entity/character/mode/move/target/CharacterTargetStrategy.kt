package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.map.Tile

data class CharacterTargetStrategy(
    private val entity: Character
) : TargetStrategy {
    override val bitMask = 0
    override val tile: Tile
        get() = entity.tile
    override val width: Int
        get() = entity.size.width
    override val height: Int
        get() = entity.size.height
    override val rotation = 0
    override val exitStrategy = -2
    override val sizeX: Int = if (entity is NPC) entity.def.size else 1
    override val sizeY: Int = if (entity is NPC) entity.def.size else 1
}