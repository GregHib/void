package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld
import world.gregs.voidps.engine.map.collision.and

/**
 * Checks for collision in the direction of movement for entities of size 1x1
 * If direction of movement is diagonal then both horizontal and vertical directions are checked too.
 */
object SmallTraversal : TileTraversalStrategy {

    override fun blocked(collision: CollisionStrategyOld, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean {
        val collisions = collision.collisions
        return when (direction) {
            Direction.NONE -> collisions.check(x, y, plane, 2359552)
            Direction.NORTH -> collisions.check(x, y + 1, plane, 2359584)
            Direction.EAST -> collisions.check(x + 1, y, plane, 2359680)
            Direction.SOUTH -> collisions.check(x, y - 1, plane, 2359554)
            Direction.WEST -> collisions.check(x - 1, y, plane, 2359560)
            Direction.NORTH_WEST -> collisions.check(x - 1, y + 1, plane, 2359608) || collisions.check(x, y + 1, plane, 2359584) || collisions.check(x - 1, y, plane, 2359560)
            Direction.NORTH_EAST -> collisions.check(x + 1, y + 1, plane, 2359776) || collisions.check(x, y + 1, plane, 2359584) || collisions.check(x + 1, y, plane, 2359680)
            Direction.SOUTH_EAST -> collisions.check(x + 1, y - 1, plane, 2359683) || collisions.check(x, y - 1, plane, 2359554) || collisions.check(x + 1, y, plane, 2359680)
            Direction.SOUTH_WEST -> collisions.check(x - 1, y - 1, plane, 2359566) || collisions.check(x, y - 1, plane, 2359554) || collisions.check(x - 1, y, plane, 2359560)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(7 and 0x7)
        for(d in Direction.values()) {
            println("$d ${d.ordinal} ${d.and() or CollisionFlag.BLOCKED}")
        }
    }
}