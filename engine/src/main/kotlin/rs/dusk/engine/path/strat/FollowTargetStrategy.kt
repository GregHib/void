package rs.dusk.engine.path.strat

import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.TargetStrategy

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