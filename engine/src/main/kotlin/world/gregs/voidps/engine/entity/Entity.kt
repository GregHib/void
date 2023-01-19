package world.gregs.voidps.engine.entity

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Overlap.isUnder
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get

/**
 * An identifiable object with a physical spatial location
 */
interface Entity {
    var tile: Tile
    val size: Size // Entity contains size so that archery objects can be targeted in combat
    val events: Events
    var values: Values?

    fun withinDistance(other: Entity, distance: Int) =
        tile.distanceTo(other) <= distance

    fun withinSight(other: Entity, walls: Boolean = false, ignore: Boolean = false) = if (walls) {
        get<LineValidator>().hasLineOfSight(tile.x, tile.y, tile.plane, other.tile.x, other.tile.y, size.width, other.size.width, other.size.height)
    } else {
        get<LineValidator>().hasLineOfWalk(tile.x, tile.y, tile.plane, other.tile.x, other.tile.y, size.width, other.size.width, other.size.height)
    }

    @Deprecated("Temp")
    fun reached(tile: Tile, size: Size): Boolean {
        return true
    }

    fun under(entity: Entity) = under(entity.tile, entity.size)

    fun under(target: Tile, targetSize: Size) = isUnder(tile, size, target, targetSize)

}