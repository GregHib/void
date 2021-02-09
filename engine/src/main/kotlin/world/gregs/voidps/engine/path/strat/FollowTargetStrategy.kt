package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.path.TargetStrategy

/**
 * Checks if on the tile behind a player
 * @author GregHib <greg@gregs.world>
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