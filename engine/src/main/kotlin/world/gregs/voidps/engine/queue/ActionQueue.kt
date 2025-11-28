package world.gregs.voidps.engine.queue

import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.resumeSuspension
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume

class ActionQueue(
    private val character: Character,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            throwable.printStackTrace()
        }
    }

    private val pending = ConcurrentLinkedQueue<Action<*>>()
    private val queue = ConcurrentLinkedQueue<Action<*>>()
    private var action: Action<*>? = null

    fun isEmpty() = queue.isEmpty() && pending.isEmpty()

    fun add(action: Action<*>): Boolean = pending.add(action)

    fun tick() {
        queuePending()
        if (queue.any { it.priority == ActionPriority.Strong }) {
            (character as? Player)?.closeMenu()
            clearWeak()
        }
        while (queue.isNotEmpty()) {
            if (!queue.removeIf(::processed)) {
                break
            }
        }
        if (character.suspension == null) {
            action = null
        }
    }

    private fun queuePending() {
        if (pending.isNotEmpty()) {
            for (action in pending) {
                queue.add(action)
            }
            pending.clear()
        }
    }

    fun contains(priority: ActionPriority): Boolean = queue.any { it.priority == priority } || pending.any { it.priority == priority }

    fun contains(name: String): Boolean = queue.any { it.name == name } || pending.any { it.name == name }

    fun clearWeak() {
        clear(ActionPriority.Weak)
    }

    fun clear(priority: ActionPriority) {
        val current = action?.priority?.ordinal
        if (current != null && current <= priority.ordinal) {
            character.suspension = null
            action = null
        }
        pending.removeIf { it.priority.ordinal <= priority.ordinal }
        queue.removeIf {
            if (it.priority.ordinal <= priority.ordinal) {
                it.cancel()
                return@removeIf true
            }
            false
        }
    }

    fun clear(name: String): Boolean = queue.removeIf { it.name == name } || pending.removeIf { it.name == name }

    fun clear() {
        queue.removeIf {
            it.cancel()
            true
        }
        pending.clear()
    }

    private fun processed(action: Action<*>): Boolean {
        if (action.priority.closeInterfaces) {
            (character as? Player)?.closeInterfaces()
        }
        if (canProcess(action) && action.process()) {
            scope.launch(action)
        }
        return action.removed
    }

    private fun canProcess(action: Action<*>) = action.priority == ActionPriority.Soft || (noDelay() && noInterrupt())

    private fun noDelay() = !character.contains("delay")

    private fun noInterrupt() = character is NPC || (character is Player && !character.hasMenuOpen() && character.dialogue == null)

    private fun CoroutineScope.launch(action: Action<*>) {
        if (character.resumeSuspension() || (character is Player && character.dialogueSuspension != null)) {
            return
        }
        val suspension = action.suspension
        if (suspension != null) {
            action.suspension = null
            suspension.resume(Unit)
            return
        }
        launch(errorHandler) {
            try {
                this@ActionQueue.action = action
                action.action.invoke(action)
            } finally {
                character.suspension = null
                action.cancel(false)
            }
        }
    }

    fun logout() {
        if (action?.behaviour == LogoutBehaviour.Accelerate) {
            character.suspension?.resume()
        }
        queuePending()
        queue.removeIf {
            if (it.behaviour == LogoutBehaviour.Accelerate) {
                scope.launch(it)
                while (character.delay != null) {
                    character.delay?.resume(Unit)
                    character.delay = null
                }
            }
            it.cancel()
            true
        }
    }
}

fun <C : Character> C.queue(name: String, initialDelay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, onCancel: (() -> Unit)? = { clearAnim() }, block: suspend Action<C>.() -> Unit) {
    queue.add(Action(this, name, ActionPriority.Normal, initialDelay, behaviour, onCancel = onCancel, action = block as suspend Action<*>.() -> Unit))
}

fun <C : Character> C.softQueue(
    name: String,
    initialDelay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    onCancel: (() -> Unit)? = { clearAnim() },
    block: suspend Action<C>.() -> Unit,
) {
    queue.add(Action(this, name, ActionPriority.Soft, initialDelay, behaviour, onCancel = onCancel, action = block as suspend Action<*>.() -> Unit))
}

fun <C : Character> C.weakQueue(
    name: String,
    initialDelay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    onCancel: (() -> Unit)? = { clearAnim() },
    block: suspend Action<C>.() -> Unit,
) {
    queue.add(Action(this, name, ActionPriority.Weak, initialDelay, behaviour, onCancel = onCancel, action = block as suspend Action<*>.() -> Unit))
}

fun <C : Character> C.strongQueue(
    name: String,
    initialDelay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    onCancel: (() -> Unit)? = { clearAnim() },
    block: suspend Action<C>.() -> Unit,
) {
    queue.add(Action(this, name, ActionPriority.Strong, initialDelay, behaviour, onCancel = onCancel, action = block as suspend Action<*>.() -> Unit))
}
