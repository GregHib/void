package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Inside a building with at least one floor above
 */
class RoofCollision(
    collisions: Collisions,
    private val land: LandCollision
) : CollisionStrategy(collisions) {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        for (z in plane + 1..3) {
            if (land.blocked(x, y, z, direction)) {
                return false
            }
        }
        return true
    }
}