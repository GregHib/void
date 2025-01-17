package world.gregs.voidps.engine.queue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.suspend.SuspendableContext

class Action<C : Character>(
    override val character: C,
    val name: String,
    val priority: ActionPriority,
    delay: Int = 0,
    val behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    override var onCancel: (() -> Unit)? = { character.clearAnimation() },
    var action: suspend Action<*>.() -> Unit = {}
) : SuspendableContext<C> {
    var suspension: CancellableContinuation<Unit>? = null
    var remaining: Int = delay
        private set

    var count = 0
        private set

    var removed = false
        private set

    /**
     * Executes action once delay has reached zero
     * @return if action was executed this call
     */
    fun process(): Boolean {
        return !removed && this.remaining != -1 && --this.remaining <= 0
    }

    suspend fun pause(ticks: Int = 1) {
        suspendCancellableCoroutine {
            remaining = ticks
            removed = false
            count++
            suspension = it
        }
    }

    fun cancel(invoke: Boolean = true) {
        remaining = -1
        removed = true
        if (invoke) {
            onCancel?.invoke()
            onCancel = null
        }
    }

    override fun toString(): String {
        return "${name}_${count}_${priority.name.toSnakeCase()}_${behaviour.name.toSnakeCase()}"
    }
}