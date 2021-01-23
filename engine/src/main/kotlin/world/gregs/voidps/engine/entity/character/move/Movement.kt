package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitInterfaces
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathFinder.Companion.getStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.TraversalStrategy
import world.gregs.voidps.engine.task.TaskExecutor
import world.gregs.voidps.engine.task.sync
import world.gregs.voidps.utility.get
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

fun Player.walkTo(target: Any, strategy: TargetStrategy = getStrategy(target), action: (PathResult) -> Unit) = get<TaskExecutor>().sync {
    dialogues.clear()
    action(ActionType.Movement) {
        try {
            val player = this@walkTo
            val path: PathFinder = get()
            retry@ while (true) {
                if (strategy.reached(player.tile, size)) {
                    action(PathResult.Success(tile))
                    break
                } else {
                    movement.clear()
                    val result = path.find(player, strategy)
                    if (result is PathResult.Failure) {
                        action(result)
                        break
                    }

                    // Await until reached the end of the path
                    while (delay(0) && awaitInterfaces()) {
                        if (movement.steps.isEmpty()) {
                            break
                        }
                        if ((target as? Character)?.action?.type == ActionType.Movement) {
                            continue@retry
                        }
                    }

                    if (result is PathResult.Partial) {
                        action(result)
                        break
                    }
                }
            }
        } finally {
            movement.clear()
        }
    }
}