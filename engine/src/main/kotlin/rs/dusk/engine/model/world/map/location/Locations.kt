package rs.dusk.engine.model.world.map.location

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import org.koin.dsl.module
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Locations(val delegate: Multimap<Tile, Location> = HashMultimap.create()) :
    Multimap<Tile, Location> by delegate

val locationModule = module {
    single { Locations() }
    single { LocationReader(get()) }
}