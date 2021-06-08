package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.algorithm.Bresenhams
import world.gregs.voidps.utility.get

/**
 * Checks if within distance of a target
 */
data class CombatTargetStrategy(
    private val target: Character,
    private var attackDistance: Int,
    private var closeCombat: Boolean
) : TileTargetStrategy {

    override val tile: Tile
        get() = target.tile
    override val size: Size
        get() = target.size

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return isWithinAttackDistance(currentX, currentY, plane, target, attackDistance, closeCombat)
    }

    companion object {
        /**
         * @param walls ranged, magic or halberds
         */
        fun isWithinAttackDistance(x: Int, y: Int, plane: Int, target: Character, attackDistance: Int, walls: Boolean): Boolean {
            val targetX = getNearest(target.tile.x, target.size.width, x)
            val targetY = getNearest(target.tile.y, target.size.height, y)
            if (Distance.chebyshev(x, y, targetX, targetY) > attackDistance) {
                return false
            }
            if (!get<Bresenhams>().withinSight(x, y, plane, targetX, targetY, target.tile.plane, walls)) {
                return false
            }
            return true
        }
    }
}