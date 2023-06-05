package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.map.Tile

/**
 * An identifiable object with a physical spatial location
 */
interface Entity {
    var tile: Tile
    val size: Size // Entity contains size so that archery objects can be targeted in combat
}