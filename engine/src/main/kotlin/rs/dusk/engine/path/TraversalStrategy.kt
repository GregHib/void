package rs.dusk.engine.path

import org.koin.dsl.module
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface TraversalStrategy {
    fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean

    fun blocked(tile: Tile, direction: Direction): Boolean = blocked(tile.x, tile.y, tile.plane, direction)
}

val traversalModule = module {
    single { SmallTraversal(get()) }
    single { MediumTraversal(get()) }
}