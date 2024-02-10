package world.gregs.voidps.engine.queue

import kotlinx.coroutines.*
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.resumeSuspension
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume

class ActionQueue(private val character: Character) : CoroutineScope {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            throwable.printStackTrace()
        }
    }
    override val coroutineContext = Dispatchers.Unconfined + errorHandler

    private val queue = ConcurrentLinkedQueue<Action>()
    private var action: Action? = null

    fun add(action: Action): Boolean {
        if (action.tick <= GameLoop.tick && processed(action)) {
            return true
        }
        return queue.add(action)
    }

    fun tick() {
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

    fun contains(priority: ActionPriority): Boolean = queue.any { it.priority == priority }

    fun contains(name: String): Boolean = queue.any { it.name == name }

    fun clearWeak() {
        if (action?.priority == ActionPriority.Weak) {
            character.suspension = null
            action = null
        }
        queue.removeIf {
            if (it.priority == ActionPriority.Weak) {
                it.cancel()
                return@removeIf true
            }
            false
        }
    }

    fun clear(name: String): Boolean {
        return queue.removeIf {
            it.name == name
        }
    }

    fun clear() {
        queue.removeIf {
            it.cancel()
            true
        }
    }

    private fun processed(action: Action): Boolean {
        if (action.priority.closeInterfaces) {
            (character as? Player)?.closeMenu()
        }
        if (canProcess(action) && action.process()) {
            launch(action)
        }
        return action.removed
    }

    private fun canProcess(action: Action) = action.priority == ActionPriority.Soft || (noDelay() && noInterrupt())

    private fun noDelay() = !character.hasClock("delay")

    private fun noInterrupt() = character is NPC || (character is Player && !character.hasMenuOpen() && character.dialogue == null)

    private fun launch(action: Action) {
        if (character.resumeSuspension() || (character is Player && character.dialogueSuspension != null)) {
            return
        }
        val suspension = action.suspension
        if (suspension != null) {
            action.suspension = null
            suspension.resume(Unit)
            return
        }
        launch {
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
        queue.removeIf {
            if (it.behaviour == LogoutBehaviour.Accelerate) {
                launch(it)
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

fun Character.queue(name: String, initialDelay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, onCancel: (() -> Unit)? = { clearAnimation() }, block: suspend Action.() -> Unit) {
    queue.add(Action(this, name, ActionPriority.Normal, initialDelay, behaviour, onCancel = onCancel, action = block))
}

fun Character.softQueue(
    name: String,
    initialDelay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    onCancel: (() -> Unit)? = { clearAnimation() },
    block: suspend Action.() -> Unit
) {
    queue.add(Action(this, name, ActionPriority.Soft, initialDelay, behaviour, onCancel = onCancel, action = block))
}

fun Character.weakQueue(
    name: String,
    initialDelay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    onCancel: (() -> Unit)? = { clearAnimation() },
    block: suspend Action.() -> Unit
) {
    queue.add(Action(this, name, ActionPriority.Weak, initialDelay, behaviour, onCancel = onCancel, action = block))
}

fun Character.strongQueue(
    name: String,
    initialDelay: Int = 0,
    behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    onCancel: (() -> Unit)? = { clearAnimation() },
    block: suspend Action.() -> Unit
) {
    queue.add(Action(this, name, ActionPriority.Strong, initialDelay, behaviour, onCancel = onCancel, action = block))
}