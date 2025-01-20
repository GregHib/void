package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.type.Tile
import kotlin.coroutines.resume

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
            val any = character.remove<Any>("face_entity")!!
            if (any is Entity) {
                if (any !is Character || character.watching(any)) {
                    character.clearWatch()
                }
                character.face(any)
            } else if (any is Tile) {
                character.clearWatch()
                character.face(any)
            }
        }
    }

    protected fun checkDelay(character: Character) {
        if (!character.contains("delay")) {
            return
        }
        val tick = character["delay", -1]
        if (tick == 1) {
            character.clear("delay")
            val delay = character.delay
            if (delay != null) {
                character.delay = null
                delay.resume(Unit)
            }
        } else if (tick > 0) {
            character["delay"] = tick - 1
        } else {
            character.clear("delay")
        }
    }

    companion object {
        const val DEBUG = false
    }
}