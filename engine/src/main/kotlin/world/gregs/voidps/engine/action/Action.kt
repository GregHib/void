package world.gregs.voidps.engine.action

import kotlinx.coroutines.*
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Events
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A suspendable action
 * Also access for suspension methods
 */
@Suppress("UNCHECKED_CAST")
class Action(
    private val events: Events
) {

    var continuation: CancellableContinuation<*>? = null
    var suspension: Suspension? = null
    var job: Job? = null
    var completion: (() -> Unit)? = null

    val isActive: Boolean
        get() = continuation?.isActive ?: true
    var type: ActionType = ActionType.None

    /**
     * Whether there is currently an action which is paused
     */
    fun isSuspended(): Boolean {
        return continuation != null && suspension != null
    }

    fun resume() = resume(Unit)

    /**
     * Resumes the current paused coroutine (if exists)
     * @param value A result to pass back to the coroutine (if applicable else [Unit])
     */
    fun <T : Any> resume(value: T) = runBlocking {
        val cont = continuation as? CancellableContinuation<T>
        if (cont != null) {
            continuation = null
            suspension = null
            cont.resume(value)
        }
    }

    fun cancel(expected: ActionType) {
        if (type == expected) {
            cancel()
        }
    }

    /**
     * Cancel the current coroutine
     * @param throwable The reason for cancellation see [ActionType]
     */
    fun cancel(throwable: CancellationException = CancellationException()) {
        job?.cancel(throwable)
        continuation?.resumeWithException(throwable)
        continuation = null
        suspension = null
    }

    /**
     * Cancels any existing action replacing it with [action]
     * @param type For the current action to decide whether to finish or cancel early
     * @param action The suspendable action function
     */
    fun run(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) {
        this@Action.cancel()
        this.type = type
        events.emit(ActionStarted(type))
        job = GlobalScope.launch(Contexts.Game) {
            this@Action.type = type
            try {
                action.invoke(this@Action)
            } finally {
                if (this@Action.type == type) {
                    this@Action.type = ActionType.None
                }
                completion?.invoke()
                completion = null
                events.emit(ActionFinished(type))
            }
        }
    }

    /**
     * Pauses the current coroutine
     * @param suspension For external systems to identify why the current coroutine is paused
     * @return The resumed result
     */
    suspend fun <T> await(suspension: Suspension) = suspendCancellableCoroutine<T> {
        continuation = it
        this.suspension = suspension
    }

    /**
     * Delays the coroutine by [ticks] ticks.
     * @return always true
     */
    suspend fun delay(ticks: Int = 1): Boolean {
        repeat(ticks) {
            suspension = Suspension.Tick
            GameLoop.await()
        }
        return true
    }
}

fun Character.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) {
    this.action.run(type, action)
}