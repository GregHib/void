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
            val locked = player.movement.frozen || !player.viewport.loaded
            if (!locked) {
                val movement = player.movement
                val steps = movement.steps
                if (steps.isEmpty()) {
                    val callback = movement.callback
                    if (callback != null) {
                        callback.invoke()
                        movement.callback = null
                    }
                }
            }
        }
    }
}