package world.gregs.voidps.engine.queue

import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.resumeSuspension
import java.util.concurrent.ConcurrentLinkedQueue

class ActionQueue(private val character: Character) : CoroutineScope {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            throwable.printStackTrace()
        }
    }
    override val coroutineContext = Dispatchers.Unconfined + errorHandler

    private val queue = ConcurrentLinkedQueue<Action>()
    private var behaviour: LogoutBehaviour? = null
    private var priority: ActionPriority? = null

    fun add(action: Action): Boolean {
        if (action.delay <= 0 && processed(action)) {
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
            behaviour = null
            priority = null
        }
    }

    fun contains(priority: ActionPriority): Boolean = queue.any { it.priority == priority }

    fun contains(name: String): Boolean = queue.any { it.name == name }

    fun clearWeak() {
        if (priority == ActionPriority.Weak) {
            character.suspension = null
        }
        queue.removeIf {
            if (it.priority == ActionPriority.Weak) {
                it.cancel()
                return@removeIf true
            }
            false
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

    private fun noInterrupt() = character is NPC || (character is Player && !character.hasScreenOpen())

    private fun launch(action: Action) {
        if (character.resumeSuspension() || (character is Player && character.dialogueSuspension != null)) {
            return
        }
        launch {
            try {
                behaviour = action.behaviour
                priority = action.priority
                action.action.invoke(action)
            } finally {
                character.suspension = null
                action.cancel(false)
            }
        }
    }

    fun logout() {
        if (behaviour == LogoutBehaviour.Accelerate) {
            character.suspension?.resume()
        }
        queue.removeIf {
            if (it.behaviour == LogoutBehaviour.Accelerate) {
                launch(it)
            }
            it.cancel()
            true
        }
    }
}

fun NPC.queue(name: String, initialDelay: Int = 0, block: suspend NPCAction.() -> Unit) {
    queue.add(NPCAction(this, name, ActionPriority.Normal, initialDelay, action = block))
}

fun NPC.strongQueue(name: String, initialDelay: Int = 0, block: suspend NPCAction.() -> Unit) {
    queue.add(NPCAction(this, name, ActionPriority.Strong, initialDelay, action = block))
}

fun NPC.softQueue(name: String, initialDelay: Int = 0, block: suspend NPCAction.() -> Unit) {
    queue.add(NPCAction(this, name, ActionPriority.Soft, initialDelay, action = block))
}

fun Player.queue(name: String, initialDelay: Int = 0, onCancel: (() -> Unit)? = { clearAnimation() }, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, name, ActionPriority.Normal, initialDelay, onCancel = onCancel, action = block))
}

fun Player.softQueue(name: String, initialDelay: Int = 0, onCancel: (() -> Unit)? = { clearAnimation() }, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, name, ActionPriority.Soft, initialDelay, onCancel = onCancel, action = block))
}

fun Player.weakQueue(name: String, initialDelay: Int = 0, onCancel: (() -> Unit)? = { clearAnimation() }, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, name, ActionPriority.Weak, initialDelay, onCancel = onCancel, action = block))
}

fun Player.strongQueue(name: String, initialDelay: Int = 0, onCancel: (() -> Unit)? = { clearAnimation() }, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, name, ActionPriority.Strong, initialDelay, onCancel = onCancel, action = block))
}