package rs.dusk.engine.model.world.map.location

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */

val locationModule = module {
    single { LocationReader(get()) }
}