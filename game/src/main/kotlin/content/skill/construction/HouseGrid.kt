package content.skill.construction

import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Houses are a [HOUSE_SIZE] grid of zones, a [ROOM_GRID] area of rooms around the [START_ROOM] with space for a ring of grass.
 * Rooms are on [GROUND_LEVEL] and [UPPER_LEVEL] with the dungeon on [DUNGEON_LEVEL]
 */
object HouseGrid {
    const val ROOM_GRID = 7
    const val HOUSE_SIZE = ROOM_GRID + 2
    const val HOUSE_CENTRE = ROOM_GRID / 2
    const val DUNGEON_LEVEL = 0
    const val GROUND_LEVEL = 1
    const val UPPER_LEVEL = 2
    const val START_ROOM = HOUSE_CENTRE + HOUSE_CENTRE * ROOM_GRID + GROUND_LEVEL * ROOM_GRID * ROOM_GRID

    fun roomPosition(x: Int, y: Int, level: Int) = x + y * ROOM_GRID + level * ROOM_GRID * ROOM_GRID

    fun roomX(position: Int) = position % ROOM_GRID

    fun roomY(position: Int) = position / ROOM_GRID % ROOM_GRID

    fun roomLevel(position: Int) = position / (ROOM_GRID * ROOM_GRID)

    fun roomAbove(position: Int) = position + ROOM_GRID * ROOM_GRID

    fun roomBelow(position: Int) = position - ROOM_GRID * ROOM_GRID

    /**
     * The zone the room at [position] occupies in the house starting at [base]
     */
    fun roomZone(base: Zone, position: Int): Zone = base.add(roomX(position) + 1, roomY(position) + 1, roomLevel(position))

    /**
     * The room position of [zone] in the house starting at [base], or null if it's outside the room grid
     */
    fun roomPosition(base: Zone, zone: Zone): Int? {
        val x = zone.x - base.x - 1
        val y = zone.y - base.y - 1
        val level = zone.level - base.level
        if (x !in 0 until ROOM_GRID || y !in 0 until ROOM_GRID || level !in 0 until 4) {
            return null
        }
        return roomPosition(x, y, level)
    }

    /**
     * Side of the room the wall, door or window on [tile] is on
     */
    fun roomSide(tile: Tile): Direction {
        val zone = tile.zone.tile
        return when {
            tile.x == zone.x -> Direction.WEST
            tile.y == zone.y -> Direction.SOUTH
            tile.x == zone.x + 7 -> Direction.EAST
            else -> Direction.NORTH
        }
    }
}
