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

    override fun run(character: Player) {
        val delay = character.delay
        if (!character.hasClock("delay") && delay != null) {
            character.delay = null
            delay.resume(Unit)
        }
        character.queue.tick()
        if (!character.hasClock("delay") && !character.hasMenuOpen()) {
            character.timers.run()
        }
        character.softTimers.run()
        character.mode.tick()
        checkTileFacing(character)
    }
}