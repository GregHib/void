package rs.dusk.engine.path.strat

import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.TargetStrategy

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