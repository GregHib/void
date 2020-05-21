package rs.dusk.engine.path

import org.koin.dsl.module
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.path.obstruction.LargeObstruction
import rs.dusk.engine.path.obstruction.MediumObstruction
import rs.dusk.engine.path.obstruction.SmallObstruction

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface ObstructionStrategy {
    fun obstructed(x: Int, y: Int, plane: Int, direction: Direction): Boolean
}

val obstructionModule = module {
    single { SmallObstruction(get()) }
    single { MediumObstruction(get()) }
    single { LargeObstruction(get()) }
}