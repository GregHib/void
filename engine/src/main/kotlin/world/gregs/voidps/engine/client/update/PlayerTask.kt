package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class PlayerTask(
    iterator: TaskIterator<Player>,
    override val characters: CharacterList<Player>
) : CharacterTask<Player>(iterator) {

    override fun run(player: Player) {
        player.queue.tick()
        if (!player.hasClock("delay") && !player.hasScreenOpen()) {
            player.timers.run()
        }
        player.softTimers.run()
        player.mode.tick()
        checkTileFacing(player)
    }
}