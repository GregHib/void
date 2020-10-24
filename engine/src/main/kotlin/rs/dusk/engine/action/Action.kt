package rs.dusk.engine.action

import kotlinx.coroutines.*
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.npc.NPCEvent
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.delay
import rs.dusk.utility.get
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

    val isActive: Boolean
        get() = continuation?.isActive ?: true
    var type: ActionType = ActionType.Misc

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
        if(type == expected) {
            cancel()
        }
    }

    /**
     * Cancel the current coroutine
     * @param throwable The reason for cancellation see [ActionType]
     */
    fun cancel(throwable: Throwable = CancellationException()) {
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
        this.type = type
        this@Action.cancel(type)
        val coroutine = action.createCoroutine(this@Action, ActionContinuation)
        scope.launch {
            this@Action.delay(0)
            coroutine.resume(Unit)
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
        val executor: TaskExecutor = get()
        suspendCancellableCoroutine<Unit> { continuation ->
            suspension = Suspension.Tick
            this.continuation = continuation
            executor.delay(ticks) {
                if(suspension == Suspension.Tick) {
                    resume()
                }
            }
        }
        return true
    }

    companion object {
        private val scope = CoroutineScope(Contexts.Game)
    }
}

fun NPCEvent.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) = npc.action(type, action)

fun PlayerEvent.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) =
    player.action(type, action)

fun Character.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) {
    this.action.run(type, action)
}