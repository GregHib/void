package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

/**
 * Checks if on an exact tile
 */
data class EntityTileTargetStrategy(
    val entity: Entity
) : TileTargetStrategy {

    override val tile: Tile
        get() = entity.tile

    override val size: Size
        get() = when (entity) {
            is GameObject -> entity.size
            is Character -> entity.size
            else -> Size.ONE
        }

    override fun reached(current: Tile, size: Size): Boolean {
        return tile == current
    }
}