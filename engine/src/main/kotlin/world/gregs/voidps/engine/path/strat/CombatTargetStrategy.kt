package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.algorithm.BresenhamsLine
import world.gregs.voidps.engine.utility.get

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
        return isWithinAttackDistance(currentX, currentY, plane, size, target, attackDistance, closeCombat)
    }

    companion object {
        fun isWithinAttackDistance(source: Character, target: Character, attackDistance: Int, walls: Boolean): Boolean {
            return isWithinAttackDistance(source.tile.x, source.tile.y, source.tile.plane, source.size, target, attackDistance, walls)
        }

        fun isUnder(tile: Tile, size: Size, target: Tile, tSize: Size) = isUnder(tile.x, tile.y, size, target.x, target.y, tSize)

        fun isUnder(x: Int, y: Int, size: Size, tX: Int, tY: Int, tSize: Size): Boolean {
            if (tX > x + size.width - 1) {
                return false
            }
            if (tY > y + size.height - 1) {
                return false
            }
            if (x > tX + tSize.width - 1) {
                return false
            }
            if (y > tY + tSize.height - 1) {
                return false
            }
            return true
        }

        fun isDiagonal(x: Int, y: Int, size: Size, tX: Int, tY: Int, tSize: Size): Boolean {
            if (x >= tX + tSize.width && y >= tY + tSize.height) {
                return true// ne
            }
            if (x + size.width <= tX && y >= tY + tSize.height) {
                return true// nw
            }
            if (x >= tX + tSize.width && y + size.height <= tY) {
                return true// se
            }
            if (x + size.width <= tX && y + size.height <= tY) {
                return true// sw
            }
            return false
        }

        /**
         * @param walls ranged, magic or halberds
         */
        fun isWithinAttackDistance(x: Int, y: Int, plane: Int, size: Size, target: Character, attackDistance: Int, walls: Boolean): Boolean {
            if (isUnder(x, y, size, target.tile.x, target.tile.y, target.size)) {
                return false
            }
            val targetX = getNearest(target.tile.x, target.size.width, x)
            val targetY = getNearest(target.tile.y, target.size.height, y)
            val sourceX = getNearest(x, size.width, targetX)
            val sourceY = getNearest(y, size.height, targetY)
            if (Distance.chebyshev(sourceX, sourceY, targetX, targetY) > attackDistance) {
                return false
            }
            if (!get<BresenhamsLine>().withinSight(sourceX, sourceY, plane, targetX, targetY, target.tile.plane, walls)) {
                return false
            }
            if (attackDistance <= 1 && isDiagonal(x, y, size, target.tile.x, target.tile.y, target.size)) {
                return false
            }
            return true
        }
    }
}