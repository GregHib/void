package rs.dusk.engine.client.update.task.player

import rs.dusk.engine.entity.character.move.PlayerMoved
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerMoveType
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.update.visual.player.movementType
import rs.dusk.engine.entity.character.update.visual.player.temporaryMoveType
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.Priority.PLAYER_MOVEMENT
import rs.dusk.engine.map.Tile
import rs.dusk.engine.tick.task.EngineTask

/**
 * Changes the tile players are located on based on [Movement.delta] and [Movement.steps]
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PlayerMovementTask(private val players: Players, private val bus: EventBus) : EngineTask(PLAYER_MOVEMENT) {

    override fun run() {
        players.forEach { player ->
            val locked = player.movement.frozen || !player.viewport.loaded
            if (!locked) {
                step(player)
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
        if (steps.peek() != null) {
            var step = steps.poll()
            if (!movement.traversal.blocked(player.tile, step)) {
                movement.walkStep = step
                movement.delta = step.delta
                player.movementType = PlayerMoveType.Walk
                player.temporaryMoveType = PlayerMoveType.Walk
                if (movement.running) {
                    if (steps.peek() != null) {
                        val tile = player.tile.add(step.delta)
                        step = steps.poll()
                        if (!movement.traversal.blocked(tile, step)) {
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

    /**
     * Moves the player tile and emits Moved event
     */
    fun move(player: Player) {
        val movement = player.movement
        movement.lastTile = player.tile
        if (movement.delta != Tile.EMPTY) {
            val from = player.tile
            player.tile = player.tile.add(movement.delta)
            bus.emit(PlayerMoved(player, from, player.tile))
        }
    }
}