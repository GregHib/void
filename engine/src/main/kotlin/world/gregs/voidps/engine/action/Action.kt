package world.gregs.voidps.engine.action

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.Instruction
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A suspendable action
 * Also access for suspension methods
 */
@Suppress("UNCHECKED_CAST")
class Action(
    private val events: Events,
    private val scope: CoroutineScope = CoroutineScope(Contexts.Game)
) {

    var continuation: CancellableContinuation<*>? = null
    var suspension: Suspension? = null
    var instruction: Instruction? = null
    var wait: Job? = null
    var job: Job? = null
    var completion: (() -> Unit)? = null

    val isActive: Boolean
        get() = continuation?.isActive ?: true
    var type: ActionType = ActionType.None
    private val logger = InlineLogger()

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

    suspend fun cancelAndJoin(throwable: CancellationException = CancellationException()) {
        job?.cancelAndJoin()
        continuation?.resumeWithException(throwable)
        continuation = null
        suspension = null
    }

    /**
     * Cancels any existing action replacing it with [action]
     * @param type For the current action to decide whether to finish or cancel early
     * @param action The suspendable action function
     */
    fun run(type: ActionType, action: suspend Action.() -> Unit) = sync {
        scope.launch {
            wait?.cancel()
            wait = this.coroutineContext.job
            this@Action.cancelAndJoin()
            this@Action.type = type
            events.emit(ActionStarted(type))
            this@Action.job = this.coroutineContext.job
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
        if (ticks <= 0) {
            return true
        }
        suspension = Suspension.Tick
        get<Scheduler>().await(ticks)
        return true
    }

    suspend fun Character.playAnimation(id: String, override: Boolean = false) {
        val ticks = setAnimation(id, override = override)
        if (ticks == -1) {
            logger.warn { "No animation delay $id" }
        } else {
            delay(ticks)
        }
    }
}

fun Character.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) {
    this.action.run(type, action = action)
}