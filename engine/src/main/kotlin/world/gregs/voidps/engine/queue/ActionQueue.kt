package world.gregs.voidps.engine.queue

import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension

class ActionQueue<C : Character>(
    private val character: C,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            throwable.printStackTrace()
        }
    }
    private val queue = ActionList<C>()
    private val weakQueue = ActionList<C>()
    private val engineQueue = ActionList<C>()

    fun isEmpty() = queue.isEmpty()

    @Suppress("UNCHECKED_CAST")
    fun add(action: Action<*>): Boolean = when (action.priority) {
        ActionPriority.Weak -> weakQueue.add(action as Action<C>)
        ActionPriority.Engine -> engineQueue.add(action as Action<C>)
        else -> queue.add(action as Action<C>)
    }

    fun tick() {
        if (queue.contains(ActionPriority.Strong)) {
            (character as? Player)?.closeMenu()
            weakQueue.clear()
        }
        process(queue)
        process(weakQueue)
    }

    private fun process(queue: ActionList<C>) {
        var action = queue.peek()
        while (action != null) {
            // Cache end to exit before the last action can add any children
            val end = action.next == null
            if (action.process() && canProcess()) {
                val next = action.next
                queue.remove(action)
                scope.launch(action)
                action = next ?: break
            }
            if (end) {
                break
            }
            action = action.next
        }
    }

    fun engineTick() {
        process(engineQueue)
    }

    fun contains(name: String): Boolean = queue.contains(name) || weakQueue.contains(name) || engineQueue.contains(name)

    fun contains(priority: ActionPriority): Boolean = queue.contains(priority) || weakQueue.contains(priority) || engineQueue.contains(priority)

    fun clearWeak() {
        clear(ActionPriority.Weak)
    }

    fun clear(priority: ActionPriority) {
        if (priority == ActionPriority.Weak) {
            weakQueue.clear()
            return
        }
        var action = queue.peek()
        while (action != null) {
            if (action.priority == priority) {
                queue.remove(action)
            }
            action = action.next
        }
    }

    fun clear(name: String): Boolean = queue.clear(name) || weakQueue.clear(name) || engineQueue.clear(name)

    fun clear() {
        queue.clear()
        weakQueue.clear()
        engineQueue.clear()
    }

    private fun canProcess() = noDelay() && noInterrupt()

    private fun noDelay() = !character.contains("delay")

    private fun noInterrupt() = character is NPC || (character is Player && !character.hasMenuOpen() && character.dialogue == null)

    private fun CoroutineScope.launch(action: Action<C>) {
        launch(errorHandler) {
            try {
                action.action.invoke(character)
            } finally {
                character.suspension = null
            }
        }
    }

    fun logout() {
        var action = queue.peek()
        while (action != null) {
            if (action.priority == ActionPriority.Long) {
                scope.launch(action)
                var suspension = character.suspension
                while (suspension != null) {
                    character.suspension = null
                    when (suspension) {
                        is Suspension.Continue -> suspension.resume()
                        is Suspension.Custom -> suspension.resume()
                        is Suspension.Delay -> suspension.resume()
                        else -> {}
                    }
                    suspension = character.suspension
                }
            }
            action = action.next
        }
        clear()
    }
}

/**
 * A normal action that waits for open interfaces before continuing
 */
fun <C : Character> C.queue(name: String, initialDelay: Int = 0, block: suspend C.() -> Unit) {
    queue.add(Action(name, initialDelay, ActionPriority.Normal, action = block))
}

/**
 * An action that can be interrupted or removed by a [strongQueue].
 */
fun Player.weakQueue(name: String, initialDelay: Int = 0, block: suspend Player.() -> Unit) {
    queue.add(Action(name, initialDelay, ActionPriority.Weak, action = block))
}

/**
 * An action that removes open interfaces and weak queues to start faster
 */
fun Player.strongQueue(name: String, initialDelay: Int = 0, block: suspend Player.() -> Unit) {
    queue.add(Action(name, initialDelay, ActionPriority.Strong, action = block))
}

/**
 * An action which is not cancelled but speeds up on logout.
 */
fun Player.longQueue(name: String, initialDelay: Int = 0, block: suspend Player.() -> Unit) {
    queue.add(Action(name, initialDelay, ActionPriority.Long, action = block))
}

/**
 * Internal queue used for actions which should happen at a different point in the tick, e.g. area changes.
 */
fun Player.engineQueue(name: String, initialDelay: Int = 0, block: suspend Player.() -> Unit) {
    queue.add(Action(name, initialDelay, ActionPriority.Engine, action = block))
}