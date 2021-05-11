package world.gregs.voidps.engine.client.update.task.player

import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.character.player.Players

/**
 * Changes the tile players are located on based on [Movement.delta] and [Movement.steps]
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class PlayerMovementCallbackTask(private val players: Players) : Runnable {

    override fun run() {
        players.forEach { player ->
            val movement = player.movement
            val locked = movement.frozen || !player.viewport.loaded
            val action = movement.action
            if (!locked && action != null && movement.steps.isEmpty()) {
                action.invoke()
                movement.action = null
            }
        }
    }
}