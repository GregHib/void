package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player

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