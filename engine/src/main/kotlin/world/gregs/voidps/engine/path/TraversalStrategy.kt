package world.gregs.voidps.engine.path

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.flag

/**
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
interface TraversalStrategy {

    fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean

    fun blocked(tile: Tile, direction: Direction): Boolean = blocked(tile.x, tile.y, tile.plane, direction)

    val type: TraversalType
    val extra: Int// Collides with entities

    /**
     * Blocked in a given direction, including any diagonals cardinals
     */
    fun Direction.block() = when (this) {
        Direction.NORTH_WEST -> CollisionFlag.NORTH_AND_WEST
        Direction.NORTH_EAST -> CollisionFlag.NORTH_AND_EAST
        Direction.SOUTH_EAST -> CollisionFlag.SOUTH_AND_EAST
        Direction.SOUTH_WEST -> CollisionFlag.SOUTH_AND_WEST
        else -> flag()
    } shl type.shift or type.block or extra

    /**
     * Blocked in any direction other than [this] and it's diagonals
     */
    fun Direction.not() = when (this) {
        Direction.NORTH_WEST -> CollisionFlag.SOUTH_AND_EAST
        Direction.NORTH -> CollisionFlag.NOT_NORTH
        Direction.NORTH_EAST -> CollisionFlag.SOUTH_AND_WEST
        Direction.EAST -> CollisionFlag.NOT_EAST
        Direction.SOUTH_EAST -> CollisionFlag.NORTH_AND_WEST
        Direction.SOUTH -> CollisionFlag.NOT_SOUTH
        Direction.SOUTH_WEST -> CollisionFlag.NORTH_AND_EAST
        Direction.WEST -> CollisionFlag.NOT_WEST
        Direction.NONE -> 0
    } shl type.shift or type.block or extra

}