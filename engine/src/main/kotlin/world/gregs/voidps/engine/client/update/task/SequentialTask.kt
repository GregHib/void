package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList

abstract class SequentialTask<C : Character> : Runnable {
    abstract val characters: CharacterList<C>

    open fun predicate(character: C): Boolean = true

    abstract fun run(character: C)

    override fun run() {
        characters.forEach {
            if (predicate(it)) {
                run(it)
            }
        }
    }
}