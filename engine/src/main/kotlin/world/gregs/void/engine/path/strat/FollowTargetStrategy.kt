package world.gregs.void.engine.path.strat

import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TargetStrategy

/**
 * Checks if on the tile behind a player
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 22, 2020
 */
data class FollowTargetStrategy(
    private val player: Player
) : TargetStrategy {

    override val tile: Tile
        get() = player.movement.previousTile

    override val size: Size
        get() = player.size

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return tile.equals(currentX, currentY, plane)
    }
}