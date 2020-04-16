package rs.dusk.engine.map.location

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import org.koin.dsl.module
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
typealias Locations = Multimap<Tile, Location>

val locationModule = module {
    single { HashMultimap.create<Tile, Location>() as Locations }
}

