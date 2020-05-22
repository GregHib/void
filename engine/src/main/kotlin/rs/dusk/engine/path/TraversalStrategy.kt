package rs.dusk.engine.path

import org.koin.dsl.module
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface TraversalStrategy {
    fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean
}

val obstructionModule = module {
    single { SmallTraversal(get()) }
    single { MediumTraversal(get()) }
}