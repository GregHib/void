package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.strategy.*
import world.gregs.voidps.engine.utility.get

interface CollisionStrategy {
    /**
     * Blocked in a given direction, including any diagonals cardinals
     */
    fun blocked(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction = Direction.NONE): Boolean

    fun blocked(collisions: Collisions, tile: Tile, direction: Direction = Direction.NONE): Boolean = blocked(collisions, tile.x, tile.y, tile.plane, direction)


    /**
     * Blocked in any direction other than [direction] and it's diagonals.
     * Note: not necessarily the same as ![blocked]
     */
    fun free(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean = !blocked(collisions, x, y, plane, direction)

    fun free(collisions: Collisions, tile: Tile, direction: Direction = Direction.NONE): Boolean = free(collisions, tile.x, tile.y, tile.plane, direction)

    fun check(collisions: Collisions, x: Int, y: Int, plane: Int, flag: Int): Boolean = collisions.check(x, y, plane, flag)
}

val Character.collision: CollisionStrategy
    get() = when (this) {
        is NPC -> when {
            def.name == "Fishing spot" -> ShoreCollision
            def["swim", false] -> SwimCollision
            def["fly", false] -> SkyCollision
            else -> NPCCollision
        }
        else -> PlayerCollision
    }

fun Character.blocked(direction: Direction): Boolean {
    return collision.blocked(get(), tile, direction)
}