package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.suspendCancellableCoroutine

object InfiniteSuspension : Suspension() {
    override val onCancel: (() -> Unit)? = null

    override fun ready(): Boolean {
        return false
    }

    context(SuspendableContext<*>) suspend operator fun invoke() {
        suspendCancellableCoroutine<Unit> {
            character.suspension = InfiniteSuspension
        }
    }
}