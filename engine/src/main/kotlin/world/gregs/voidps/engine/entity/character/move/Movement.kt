package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathFinder.Companion.getStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.TraversalStrategy
import world.gregs.voidps.engine.sync
import java.util.*

/**
 * @author GregHib <greg@gregs.world>
 * @since April 26, 2020
 */

typealias Steps = LinkedList<Direction>

data class Movement(
    var previousTile: Tile = Tile.EMPTY,
    var trailingTile: Tile = Tile.EMPTY,
    var delta: Tile = Tile.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val steps: LinkedList<Direction> = LinkedList(),
    var frozen: Boolean = false,
    var running: Boolean = false
) {

    var callback: (() -> Unit)? = null
    var target: Triple<Any, TargetStrategy, (PathResult) -> Unit>? = null
    lateinit var traversal: TraversalStrategy

    fun clear() {
        steps.clear()
        reset()
    }

    fun reset() {
        delta = Tile.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}

fun Player.walkTo(target: Any, strategy: TargetStrategy = getStrategy(target), action: (PathResult) -> Unit) {
    sync {
        movement.target = Triple(target, strategy, action)
    }
}