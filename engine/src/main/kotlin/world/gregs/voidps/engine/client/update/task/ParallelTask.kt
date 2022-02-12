package world.gregs.voidps.engine.client.update.task

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList

abstract class ParallelTask<C : Character> : Runnable {
    abstract val characters: CharacterList<C>

    open fun predicate(character: C): Boolean = true

    abstract fun run(character: C)

    override fun run() = runBlocking {
        coroutineScope {
            characters.forEach {
                if (predicate(it)) {
                    launch(Contexts.Updating) {
                        run(it)
                    }
                }
            }
        }
    }
}