package rs.dusk.engine.action

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import rs.dusk.engine.model.entity.index.player.Player
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A suspendable action
 * Also access for suspension methods
 */
@Suppress("UNCHECKED_CAST")
class Action {
    var continuation: CancellableContinuation<*>? = null
    var suspension: Suspension? = null

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
    fun <T: Any> resume(value: T) {
        val cont = continuation as? CancellableContinuation<T>
        if(cont != null) {
            continuation = null
            suspension = null
            cont.resume(value)
        }
    }

    /**
     * Cancel the current coroutine
     * @param throwable The reason for cancellation see [ActionType]
     */
    fun cancel(throwable: Throwable) {
        continuation?.resumeWithException(throwable)
    }

    /**
     * Cancels any existing action replacing it with [action]
     * @param type For the current action to decide whether to finish or cancel early
     * @param action The suspendable action function
     */
    fun run(type: ActionType, action: suspend Action.() -> Unit) {
        cancel(type)
        val coroutine = action.createCoroutine(this, QueueContinuation)
        coroutine.resume(Unit)
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
     * TODO move to interface system
     * Wait until a main interface is closed
     * @return always true
     */
    suspend fun awaitInterfaces() : Boolean {
        var playerHasInterfaceOpen = false
        if(playerHasInterfaceOpen) {
            await<Unit>(Suspension.Interfaces)
        }
        return true
    }

    /**
     * Delays the coroutine by [tick] ticks.
     * @return always true
     */
    suspend fun delay(tick: Int = 1): Boolean {
        repeat(tick) {
            await<Unit>(Suspension.Tick)
        }
        return true
    }
}

fun Player.action(type: ActionType, action: suspend Action.() -> Unit) {
    this.action.run(type, action)
}