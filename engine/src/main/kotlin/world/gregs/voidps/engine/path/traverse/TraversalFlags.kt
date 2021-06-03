package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.flag
import world.gregs.voidps.engine.path.TraversalType

/**
 * Blocked in a given direction, including any diagonals cardinals
 * @param type Method used to traverse the world
 * @param extra Entity collision
 */
fun Direction.block(type: TraversalType, extra: Int = -1): Int {
    val value = when (this) {
        Direction.NORTH_WEST -> CollisionFlag.NORTH_AND_WEST
        Direction.NORTH_EAST -> CollisionFlag.NORTH_AND_EAST
        Direction.SOUTH_EAST -> CollisionFlag.SOUTH_AND_EAST
        Direction.SOUTH_WEST -> CollisionFlag.SOUTH_AND_WEST
        else -> flag()
    } shl type.shift or type.block
    return if (extra != -1) value or extra else value
}

/**
 * Blocked in any direction other than [this] and it's diagonals
 * @param type Method used to traverse the world
 * @param extra Entity collision
 */
fun Direction.not(type: TraversalType, extra: Int = -1): Int {
    val value = when (this) {
        Direction.NORTH_WEST -> CollisionFlag.SOUTH_AND_EAST
        Direction.NORTH -> CollisionFlag.NOT_NORTH
        Direction.NORTH_EAST -> CollisionFlag.SOUTH_AND_WEST
        Direction.EAST -> CollisionFlag.NOT_EAST
        Direction.SOUTH_EAST -> CollisionFlag.NORTH_AND_WEST
        Direction.SOUTH -> CollisionFlag.NOT_SOUTH
        Direction.SOUTH_WEST -> CollisionFlag.NORTH_AND_EAST
        Direction.WEST -> CollisionFlag.NOT_WEST
        Direction.NONE -> 0
    } shl type.shift or type.block
    return if (extra != -1) value or extra else value
}