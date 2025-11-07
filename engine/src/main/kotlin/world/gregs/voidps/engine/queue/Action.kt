package world.gregs.voidps.engine.queue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class Action<C : Character>(
    val character: C,
    val name: String,
    val priority: ActionPriority,
    delay: Int = 0,
    val behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    var onCancel: (() -> Unit)? = { character.clearAnim() },
    var action: suspend Action<*>.() -> Unit = {},
) {
    var suspension: CancellableContinuation<Unit>? = null
    var remaining: Int = delay
        private set

    var count = 0
        private set

    var removed = false
        private set

    val player: Player
        get() = character as Player

    val npc: NPC
        get() = character as NPC

    /**
     * Executes action once delay has reached zero
     * @return if action was executed this call
     */
    fun process(): Boolean = !removed && this.remaining != -1 && --this.remaining <= 0

    suspend fun pause(ticks: Int) {
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

    /**
     * Queue calls shouldn't be nested and should be replaced with suspensions
     */

    @Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
    @Deprecated("Replace nested queues with pause", ReplaceWith("pause(initialDelay)"))
    fun Character.queue(name: String, initialDelay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, onCancel: (() -> Unit)? = null, block: (suspend Action<C>.() -> Unit)?) {
    }

    @Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
    @Deprecated("Replace nested queues with pause", ReplaceWith("pause(initialDelay)"))
    fun Character.softQueue(name: String, initialDelay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, onCancel: (() -> Unit)? = null, block: (suspend Action<C>.() -> Unit)?) {
    }

    @Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
    @Deprecated("Replace nested queues with pause", ReplaceWith("pause(initialDelay)"))
    fun Character.weakQueue(name: String, initialDelay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, onCancel: (() -> Unit)? = null, block: (suspend Action<C>.() -> Unit)?) {
    }

    @Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
    @Deprecated("Replace nested queues with pause", ReplaceWith("pause(initialDelay)"))
    fun Character.strongQueue(name: String, initialDelay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, onCancel: (() -> Unit)? = null, block: (suspend Action<C>.() -> Unit)?) {
    }

    override fun toString(): String = "${name}_${count}_${priority.name.toSnakeCase()}_${behaviour.name.toSnakeCase()}"
}
