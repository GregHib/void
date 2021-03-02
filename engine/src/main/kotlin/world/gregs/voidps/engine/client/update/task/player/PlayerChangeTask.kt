package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.tick.task.EntityTask
import kotlin.math.abs

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class PlayerChangeTask(override val entities: Players) : EntityTask<Player>(true) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) {
        val movement = player.movement
        val delta = movement.delta
        if (delta != Delta.EMPTY) {
            if (movement.walkStep != Direction.NONE) {
                var value = -1
                var move = movement.walkStep.delta
                player.change = LocalChange.Walk

                if (movement.runStep != Direction.NONE) {
                    value = getRunIndex(delta)
                    if (value != -1) {
                        player.change = LocalChange.Run
                    } else {
                        move = delta
                    }
                }

                if (value == -1) {
                    value = getWalkIndex(move)
                }

                player.changeValue = value
            } else {
                player.change = if (withinView(delta)) LocalChange.Tele else LocalChange.TeleGlobal
                player.changeValue = if (player.change == LocalChange.Tele) {
                    (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
                } else {
                    (delta.y and 0x3fff) + (delta.x and 0x3fff shl 14) + (delta.plane and 0x3 shl 28)
                }
            }
        } else if (player.visuals.update != null) {
            player.change = LocalChange.Update
            player.changeValue = -1
        } else {
            player.change = null
            player.changeValue = -1
        }
    }

    companion object {

        private fun withinView(delta: Delta): Boolean {
            return abs(delta.x) <= VIEW_RADIUS && abs(delta.y) <= VIEW_RADIUS
        }

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
        fun getRunIndex(delta: Delta): Int {
            for (i in RUN_X.indices) {
                if (delta.equals(RUN_X[i], RUN_Y[i])) {
                    return i
                }
            }
            return -1
        }

        private val WALK_X = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
        private val WALK_Y = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)

        /**
         * Index of movement direction
         * |05|06|07|
         * |03|PP|04|
         * |00|01|02|
         */
        fun getWalkIndex(delta: Delta): Int {
            for (i in WALK_X.indices) {
                if (delta.equals(WALK_X[i], WALK_Y[i])) {
                    return i
                }
            }
            return -1
        }
    }

}