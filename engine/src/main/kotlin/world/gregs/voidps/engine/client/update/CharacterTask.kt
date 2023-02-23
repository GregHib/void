package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.turn
import world.gregs.voidps.engine.map.Tile

abstract class CharacterTask<C : Character>(
    private val iterator: TaskIterator<C>
) : Runnable {

    abstract val characters: CharacterList<C>

    open fun predicate(character: C): Boolean = true

    abstract fun run(character: C)

    override fun run() {
        iterator.run(this)
    }

    protected fun checkTileFacing(before: Tile, character: Character) {
        if (before == character.tile && character.hasVar("face_entity")) {
            val delta = character.removeVar<Tile>("face_entity")!!.delta(character.tile)
            character.turn(delta)
        }
    }
}