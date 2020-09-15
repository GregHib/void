package rs.dusk.engine.path.strat

import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.TargetStrategy

/**
 * Checks if within distance of a target
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since September 9, 2020
 */
data class DistanceTargetStrategy(
    private val player: Player,
    private var distance: Int,
    private val delegate: TargetStrategy
) : TargetStrategy {

    override val tile: Tile
        get() = delegate.tile

    override val size: Size
        get() = delegate.size

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return delegate.tile.within(currentX, currentY, plane, distance) || delegate.reached(currentX, currentY, plane, size)
    }
}