package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.MovementMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class MovementTask<C : Character>(
    iterator: TaskIterator<C>,
    override val characters: CharacterList<C>
) : CharacterTask<C>(iterator) {

    override fun predicate(character: C): Boolean {
        return character is NPC || character is Player && character.viewport?.loaded != false
    }

    override fun run(character: C) {
        val mode = character.mode
        if (mode is MovementMode) {
            mode.tick(character)
        }
    }

    override fun run() {
        MovementMode.before()
        super.run()
        MovementMode.after()
    }
}