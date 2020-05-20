package rs.dusk.engine.path

import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface Finder {
    fun find(
        tile: Tile,
        size: Size,
        steps: Steps,
        target: Target,
        strategy: TargetStrategy,
        obstruction: ObstructionStrategy
    ): Int
}