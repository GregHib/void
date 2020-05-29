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
        val locked = movement.frozen || !player.viewport.loaded
        if (!locked && steps.peek() != null) {
            var step = steps.poll()
            if (!movement.traversal.blocked(player.tile.x, player.tile.y, player.tile.plane, step)) {
                movement.walkStep = step
                movement.delta = step.delta
                player.movementType = PlayerMoveType.Walk
                player.temporaryMoveType = PlayerMoveType.Walk
                if (movement.running) {
                    if (steps.peek() != null) {
                        val tile = player.tile.add(step.delta)
                        step = steps.poll()
                        if (!movement.traversal.blocked(tile.x, tile.y, tile.plane, step)) {
                            movement.runStep = step
                            movement.delta = movement.delta.add(step.delta)
                            player.movementType = PlayerMoveType.Run
                            player.temporaryMoveType = PlayerMoveType.Run
                        }
                    } else {
                        player.movementType = PlayerMoveType.Walk
                        player.temporaryMoveType = PlayerMoveType.Run
                    }
                }
            }
        }
    }
}