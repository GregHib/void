package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile

/**
 * An identifiable object with a physical spatial location
 */
interface Entity {
    var tile: Tile
    // Entity contains size so that archery objects can be targeted in combat
    val size: Size
    val events: Events
    val values: Values
}