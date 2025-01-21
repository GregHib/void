package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.resume

class PlayerTask(
    iterator: TaskIterator<Player>,
    override val characters: CharacterList<Player>
) : CharacterTask<Player>(iterator) {

    override fun run(character: Player) {
        checkDelay(character)
        character.queue.tick()
        if (!character.contains("delay") && !character.hasMenuOpen()) {
            character.timers.run()
        }
        character.softTimers.run()
        character.mode.tick()
        checkTileFacing(character)
    }
}