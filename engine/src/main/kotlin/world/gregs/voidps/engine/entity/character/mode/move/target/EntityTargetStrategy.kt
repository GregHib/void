package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameMapObject
import world.gregs.voidps.engine.map.Tile

data class EntityTargetStrategy(
    private val entity: Entity
) : TargetStrategy {
    override val bitMask = 0
    override val tile: Tile
        get() = entity.tile
    override val size: Size
        get() = entity.size
    override val rotation = 0
    override val exitStrategy = -2
    override val width: Int = if (entity is NPC) entity.def.size else if (entity is GameMapObject) entity.def.sizeX else 1
    override val height: Int = if (entity is NPC) entity.def.size else if (entity is GameMapObject) entity.def.sizeY else 1
}