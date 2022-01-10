package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Overlap.isUnder
import world.gregs.voidps.engine.map.Tile

/**
 * An identifiable object with a physical spatial location
 */
interface Entity {
    var tile: Tile
    val size: Size // Entity contains size so that archery objects can be targeted in combat
    val events: Events
    val values: Values

    fun under(entity: Entity) = under(entity.tile, entity.size)

    fun under(target: Tile, targetSize: Size) = isUnder(tile, size, target, targetSize)

}