package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.update.visual.player.movementType
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerMovementTask : ParallelEngineTask() {

    private val logger = InlineLogger()
    val players: Players by inject()

    override fun run() {
        players.forEach { player ->
            defers.add(updatePlayer(player))
        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "Update calculation took ${took}ms" }
        }
    }

    fun updatePlayer(player: Player) = GlobalScope.async {
        val movement = player.movement
        val delta = movement.delta

        movement.lastTile = player.tile

        player.change = when {
            delta.id != 0 && movement.runStep != Direction.NONE -> LocalChange.Run
            delta.id != 0 && movement.walkStep != Direction.NONE -> LocalChange.Walk
            delta.id != 0 && player.movementType == PlayerMoveType.Teleport -> LocalChange.Tele
            player.visuals.update != null -> LocalChange.Update
            else -> null
        }

        player.changeValue = when (player.change) {
            LocalChange.Walk -> getMovementIndex(movement.walkStep)
            LocalChange.Run -> getMovementIndex(movement.walkStep, movement.runStep)
            LocalChange.Tele -> (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
            else -> -1
        }
    }

    companion object {

        private val RUN_X = intArrayOf(-2, -1, 0, 1, 2, -2, 2, -2, 2, -2, 2, -2, -1, 0, 1, 2)
        private val RUN_Y = intArrayOf(-2, -2, -2, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 2, 2, 2)

        /**
         * Index of two combined movement directions
         * |11|12|13|14|15|
         * |09|  |  |  |10|
         * |07|  |PP|  |08|
         * |05|  |  |  |06|
         * |00|01|02|03|04|
         */
        fun getMovementIndex(first: Direction, second: Direction): Int {
            val delta = first.delta.add(second.delta)
            for (i in RUN_X.indices) {
                if (RUN_X[i] == delta.x && RUN_Y[i] == delta.y) {
                    return i
                }
            }
            return -1
        }

        private val WALK_X = intArrayOf(1, 0, -1, 1, -1, 1, 0, -1)
        private val WALK_Y = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)

        /**
         * Index of movement direction
         * |00|01|02|
         * |03|PP|04|
         * |05|06|07|
         */
        fun getMovementIndex(direction: Direction): Int {
            for (i in WALK_X.indices) {
                if (WALK_X[i] == direction.delta.x && WALK_Y[i] == direction.delta.y) {
                    return i
                }
            }
            return -1
        }
    }

}