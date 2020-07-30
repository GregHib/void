package rs.dusk.engine.entity

import rs.dusk.engine.map.Tile

/**
 * An identifiable object with a physical spatial location
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface Entity {
    val id: Int
    var tile: Tile
}