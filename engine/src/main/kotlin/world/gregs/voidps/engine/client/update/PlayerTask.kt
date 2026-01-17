package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players

class PlayerTask(
    iterator: TaskIterator<Player>,
    override val characters: Iterable<Player> = Players,
) : CharacterTask<Player>(iterator) {

    override fun run(character: Player) {
        checkDelay(character)
        character.queue.tick()
        if (!character.contains("delay") && !character.hasMenuOpen()) {
            character.timers.run()
        }
        character.softTimers.run()
//        character.area.tick()
        character.mode.tick()
        checkTileFacing(character)
    }
}
