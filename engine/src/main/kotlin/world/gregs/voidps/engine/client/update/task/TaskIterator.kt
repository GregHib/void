package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.character.Character

interface TaskIterator<C : Character> {

    fun run(task: CharacterTask<C>)

}