package world.gregs.voidps.engine.client.update.task

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.character.Character

class ParallelIterator<C : Character> : TaskIterator<C> {
    override fun run(task: CharacterTask<C>) {
        runBlocking {
            coroutineScope {
                task.characters.forEach {
                    if (task.predicate(it)) {
                        launch(Contexts.Updating) {
                            task.run(it)
                        }
                    }
                }
            }
        }
    }
}