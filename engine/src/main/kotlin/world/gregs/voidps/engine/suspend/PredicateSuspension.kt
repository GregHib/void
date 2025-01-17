package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.Character

class PredicateSuspension(
    private val predicate: () -> Boolean,
) : Suspension() {

    override fun ready(): Boolean {
        return predicate.invoke()
    }

    companion object {
        suspend fun start(character: Character, predicate: () -> Boolean) {
            val suspension = PredicateSuspension(predicate)
            suspendCancellableCoroutine {
                suspension.continuation = it
                character.suspension = suspension
            }
            character.suspension = null
        }
    }
}