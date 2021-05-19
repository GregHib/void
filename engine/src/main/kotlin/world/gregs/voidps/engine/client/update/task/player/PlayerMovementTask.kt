package world.gregs.voidps.engine.client.update.task.player

import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerMoveType
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.entity.character.update.visual.player.temporaryMoveType
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Changes the tile players are located on based on [Movement.delta] and [Movement.steps]
 */
class PlayerMovementTask(
    private val players: Players,
    private val collisions: Collisions
) : Runnable {

    override fun run() {
        players.forEach { player ->
            if (player.viewport.loaded) {
                if (!player.movement.frozen) {
                    step(player)
                }
                move(player)
            }
        }
    }

    /**
     * Sets up walk and run changes based on [Steps] queue.
     */
    fun step(player: Player) {
        val movement = player.movement
        val steps = movement.steps
        movement.moving = steps.peek() != null
        if (movement.moving) {
            var step = steps.poll()
            if (!movement.traversal.blocked(player.tile, step)) {
                movement.previousTile = player.tile
                movement.walkStep = step
                movement.delta = step.delta
                player.face(step, false)
                player.movementType = PlayerMoveType.Walk
                player.temporaryMoveType = PlayerMoveType.Walk
                if (player.running) {
                    if (steps.peek() != null) {
                        val tile = player.tile.add(step.delta)
                        step = steps.poll()
                        if (!movement.traversal.blocked(tile, step)) {
                            movement.previousTile = tile
                            movement.runStep = step
                            movement.delta = movement.delta.add(step.delta)
                            player.face(step, false)
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

    /**
     * Moves the player tile and emits Moved event
     */
    fun move(player: Player) {
        val movement = player.movement
        movement.trailingTile = player.tile
        if (movement.delta != Delta.EMPTY) {
            val from = player.tile
            player.tile = player.tile.add(movement.delta)
            players.update(from, player.tile, player)
            collisions.move(player, from, player.tile)
            player.events.emit(Moved(from, player.tile))
        }
    }
}