package world.gregs.void.engine.path.strat

import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.Character
import world.gregs.void.engine.entity.obj.GameObject
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TargetStrategy

/**
 * Checks if on an exact tile
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
data class EntityTileTargetStrategy(
    val entity: Entity
) : TargetStrategy {

    override val tile: Tile
        get() = entity.tile

    override val size: Size
        get() = when (entity) {
            is GameObject -> entity.size
            is Character -> entity.size
            else -> Size.TILE
        }

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return tile.equals(currentX, currentY, plane)
    }
}