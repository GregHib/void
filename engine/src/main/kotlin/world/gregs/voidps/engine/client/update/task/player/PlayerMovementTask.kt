package world.gregs.voidps.engine.client.update.task.player

import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerMoveType
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.entity.character.update.visual.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.traverse.traversal

/**
 * Changes the tile players are located on based on [Movement.delta] and [Movement.steps]
 */
class PlayerMovementTask(
    private val players: Players,
    private val collisions: Collisions,
    private val collision: CollisionStrategyProvider
) : Runnable {

    override fun run() {
        players.forEach { player ->
            if (player.viewport.loaded) {
                if (!player.hasEffect("frozen")) {
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
        val path = movement.path
        player.moving = path.steps.peek() != null
        if (player.moving) {
            var step = path.steps.poll()
            val collision = collision.get(player)
            if (!player.traversal.blocked(collision, player.tile, player.size, step)) {
                movement.previousTile = player.tile
                movement.walkStep = step
                movement.delta = step.delta
                player.face(step, false)
                player.movementType = PlayerMoveType.Walk
                player.temporaryMoveType = PlayerMoveType.Walk
                if (player.running) {
                    if (path.steps.peek() != null) {
                        val tile = player.tile.add(step.delta)
                        step = path.steps.poll()
                        if (!player.traversal.blocked(collision, tile, player.size, step)) {
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
            if (path.steps.isEmpty()) {
                player.events.emit(MoveStop)
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