package rs.dusk.engine.client.update.task

import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.index.update.visual.player.movementType

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerMovementTask(override val entities: Players) : EntityTask<Player>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) {
        val movement = player.movement

        val steps = movement.steps
        if (steps.peek() != null) {
            val step = steps.poll()
            // TODO if running poll twice
            movement.walkStep = step
            movement.delta = step.delta
        }

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

        private val WALK = arrayOf(
            Direction.SOUTH_WEST,
            Direction.SOUTH,
            Direction.SOUTH_EAST,
            Direction.WEST,
            Direction.EAST,
            Direction.NORTH_WEST,
            Direction.NORTH,
            Direction.NORTH_EAST
        )

        /**
         * Index of movement direction
         * |05|06|07|
         * |03|PP|04|
         * |00|01|02|
         */
        fun getMovementIndex(direction: Direction): Int {
            return WALK.indexOf(direction)
        }
    }

}