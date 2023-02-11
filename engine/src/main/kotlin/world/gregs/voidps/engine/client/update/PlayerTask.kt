package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.hasEffect
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class PlayerTask(
    iterator: TaskIterator<Player>,
    override val characters: CharacterList<Player>
) : CharacterTask<Player>(iterator) {

    override fun run(player: Player) {
        val before = player.tile
        player.queue.tick()
        if (!player.hasEffect("delay")) {
            player.normalTimers.run()
        }
        player.timers.run()
        player.mode.tick()
        checkTileFacing(before, player)
    }
}