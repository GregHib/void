package world.gregs.voidps.engine.client.update.iterator

import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.entity.character.Character

interface TaskIterator<C : Character> {

    fun run(task: CharacterTask<C>)
}
