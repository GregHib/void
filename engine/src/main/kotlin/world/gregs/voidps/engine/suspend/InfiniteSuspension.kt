package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.CharacterContext

object InfiniteSuspension : Suspension() {
    override val onCancel: (() -> Unit)? = null

    override fun ready(): Boolean {
        return false
    }

    context(CharacterContext<*>) suspend operator fun invoke() {
        suspendCancellableCoroutine<Unit> {
            character.suspension = InfiniteSuspension
        }
    }
}