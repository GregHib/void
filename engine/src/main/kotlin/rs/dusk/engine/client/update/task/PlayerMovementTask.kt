package rs.dusk.engine.client.update.task

import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.index.update.visual.player.movementType
import rs.dusk.engine.model.entity.index.update.visual.player.temporaryMoveType

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
            var step = steps.poll()
            /*
                TODO move `getTraversal` out of PathFinder and into `Entity`
                    Then use here to check if a step is still traversable.
             */
            movement.walkStep = step
            movement.delta = step.delta
            player.movementType = PlayerMoveType.Walk
            player.temporaryMoveType = PlayerMoveType.Walk

            var running = true// TODO
            if (running) {
                player.temporaryMoveType = PlayerMoveType.Run
                if (steps.peek() != null) {
                    step = steps.poll()
                    movement.runStep = step
                    movement.delta = movement.delta.add(step.delta)
                    player.movementType = PlayerMoveType.Run
                } else {
                    player.movementType = PlayerMoveType.Walk
                }
            }
        } else if (player.temporaryMoveType != PlayerMoveType.None) {
            player.temporaryMoveType = PlayerMoveType.None
            player.movementType = PlayerMoveType.None
        }
    }
}