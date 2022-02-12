package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.character.Character

class SequentialIterator<C : Character> : TaskIterator<C> {
    override fun run(task: CharacterTask<C>) {
        task.characters.forEach { character ->
            if (task.predicate(character)) {
                task.run(character)
            }
        }
    }
}