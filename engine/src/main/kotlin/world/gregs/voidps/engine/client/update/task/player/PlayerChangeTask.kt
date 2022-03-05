package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.engine.client.update.task.CharacterTask
import world.gregs.voidps.engine.client.update.task.TaskIterator
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.LocalChange
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.Delta
import kotlin.math.abs

class PlayerChangeTask(
    iterator: TaskIterator<Player>,
    override val characters: Players
) : CharacterTask<Player>(iterator) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(player: Player) {
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
        } else if (player.visuals.flag != 0) {
            player.change = LocalChange.Update
            player.changeValue = -1
        } else {
            player.change = null
            player.changeValue = -1
        }
    }

    companion object {

        fun withinView(delta: Delta): Boolean {
            return abs(delta.x) <= VIEW_RADIUS && abs(delta.y) <= VIEW_RADIUS
        }

        /**
         * Index of two combined movement directions
         * |11|12|13|14|15|
         * |09|  |  |  |10|
         * |07|  |PP|  |08|
         * |05|  |  |  |06|
         * |00|01|02|03|04|
         */
        fun getRunIndex(delta: Delta): Int = when {
            delta.x == -2 && delta.y == -2 -> 0
            delta.x == -1 && delta.y == -2 -> 1
            delta.x == 0 && delta.y == -2 -> 2
            delta.x == 1 && delta.y == -2 -> 3
            delta.x == 2 && delta.y == -2 -> 4
            delta.x == -2 && delta.y == -1 -> 5
            delta.x == 2 && delta.y == -1 -> 6
            delta.x == -2 && delta.y == 0 -> 7
            delta.x == 2 && delta.y == 0 -> 8
            delta.x == -2 && delta.y == 1 -> 9
            delta.x == 2 && delta.y == 1 -> 10
            delta.x == -2 && delta.y == 2 -> 11
            delta.x == -1 && delta.y == 2 -> 12
            delta.x == 0 && delta.y == 2 -> 13
            delta.x == 1 && delta.y == 2 -> 14
            delta.x == 2 && delta.y == 2 -> 15
            else -> -1
        }

        /**
         * Index of movement direction
         * |05|06|07|
         * |03|PP|04|
         * |00|01|02|
         */
        fun getWalkIndex(delta: Delta): Int = when {
            delta.x == -1 && delta.y == -1 -> 0
            delta.x == 0 && delta.y == -1 -> 1
            delta.x == 1 && delta.y == -1 -> 2
            delta.x == -1 && delta.y == 0 -> 3
            delta.x == 1 && delta.y == 0 -> 4
            delta.x == -1 && delta.y == 1 -> 5
            delta.x == 0 && delta.y == 1 -> 6
            delta.x == 1 && delta.y == 1 -> 7
            else -> -1
        }
    }

}