package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get

abstract class CollisionStrategy(
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

    fun check(x: Int, y: Int, plane: Int, flag: Int): Boolean = collisions.check(x, y, plane, flag)
}

val Character.collision: CollisionStrategy
    get() = get<CollisionStrategyProvider>().get(this)

fun Character.blocked(direction: Direction): Boolean {
    return get<CollisionStrategyProvider>().get(this).blocked(tile, direction)
}