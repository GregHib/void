package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.resume

class PlayerTask(
    iterator: TaskIterator<Player>,
    override val characters: CharacterList<Player>
) : CharacterTask<Player>(iterator) {

    override fun run(player: Player) {
        val delay = player.delay
        if (!player.hasClock("delay") && delay != null) {
            player.delay = null
            delay.resume(Unit)
        }
        player.queue.tick()
        if (!player.hasClock("delay") && !player.hasMenuOpen()) {
            player.timers.run()
        }
        player.softTimers.run()
        player.mode.tick()
        checkTileFacing(player)
    }
}