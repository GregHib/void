package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.flag

/**
 * Interact with a single tile from any of the 4 cardinal directions
 */
class CardinalTargetStrategy(
    private val collisions: Collisions,
    override val tile: Tile
) : TileTargetStrategy {

    override val size: Size
        get() = Size.ONE

    override fun reached(current: Tile, size: Size): Boolean {
        if (current == tile) {
            return true
        }
        val delta = tile.delta(current)
        if (!delta.isCardinal()) {
            return false
        }
        if (delta.x !in -1..1 || delta.y !in -1..1) {
            return false
        }
        return !collisions.check(current, delta.toDirection().flag())
    }
}