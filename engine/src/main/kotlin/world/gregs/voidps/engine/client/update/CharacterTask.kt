package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.remove
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.face

abstract class CharacterTask<C : Character>(
    private val iterator: TaskIterator<C>
) : Runnable {

    abstract val characters: CharacterList<C>

    open fun predicate(character: C): Boolean = true

    abstract fun run(character: C)

    override fun run() {
        iterator.run(this)
    }

    protected fun checkTileFacing(character: Character) {
        if (!character.visuals.moved && character.contains("face_entity")) {
            character.clearWatch()
            character.face(character.remove<Entity>("face_entity")!!)
        }
    }
}