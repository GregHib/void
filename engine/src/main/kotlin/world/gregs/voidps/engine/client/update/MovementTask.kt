package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.Movement
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class MovementTask<C : Character>(
    iterator: TaskIterator<C>,
    override val characters: CharacterList<C>
) : CharacterTask<C>(iterator) {

    override fun run(character: C) {
        character.mode.tick()
    }

    override fun run() {
        Movement.before()
        super.run()
        Movement.after()
    }
}