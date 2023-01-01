package world.gregs.voidps.engine.map.collision

import org.rsmod.pathfinder.StepValidator
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile

/**
 * Checks if a certain style of movement is blocked for a specific tile
 * Used in line of sight, pathfinding and movement.
 */
abstract class CollisionStrategyOld(
    internal val collisions: Collisions
) {

    /**
     * Blocked in a given direction, including any diagonals cardinals
     */
    abstract fun blocked(x: Int, y: Int, plane: Int, direction: Direction = Direction.NONE): Boolean

    fun blocked(tile: Tile, direction: Direction = Direction.NONE): Boolean = blocked(tile.x, tile.y, tile.plane, direction)

    /**
     * Blocked in any direction other than [direction] and it's diagonals.
     * Note: not necessarily the same as ![blocked]
     */
    open fun free(x: Int, y: Int, plane: Int, direction: Direction): Boolean = !blocked(x, y, plane, direction)

    fun free(tile: Tile, direction: Direction = Direction.NONE): Boolean = free(tile.x, tile.y, tile.plane, direction)

}

fun Character.blocked(direction: Direction) = blocked(tile, direction)

fun Character.blocked(tile: Tile, direction: Direction): Boolean {
    return !world.gregs.voidps.engine.utility.get<StepValidator>().canTravel(tile.x, tile.y, tile.plane, size.width, direction.delta.x, direction.delta.y, 0, collision)
}