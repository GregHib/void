package rs.dusk.engine.model.world.map.location

import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Location(val id: Int, val tile: Tile, val type: Int, val rotation: Int)