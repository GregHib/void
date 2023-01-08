package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
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
}