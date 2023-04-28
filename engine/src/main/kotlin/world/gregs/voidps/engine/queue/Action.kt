package world.gregs.voidps.engine.queue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.pearx.kasechange.toSnakeCase

abstract class Action(
    val name: String,
    val priority: ActionPriority,
    delay: Int = 0,
    val behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    var action: suspend Action.() -> Unit = {}
) {
    open var onCancel: (() -> Unit)? = null
    var suspension: CancellableContinuation<Unit>? = null
    var delay: Int = delay
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
        return !removed && this.delay-- <= 0
    }

    suspend fun pause(ticks: Int = 1) {
        suspendCancellableCoroutine {
            delay = ticks
            removed = false
            count++
            suspension = it
        }
    }

    fun cancel(invoke: Boolean = true) {
        delay = -1
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