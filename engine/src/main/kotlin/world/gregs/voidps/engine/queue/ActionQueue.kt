package world.gregs.voidps.engine.queue

import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.closeInterface
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
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

    fun add(action: Action) = queue.add(action)

    fun tick() {
        if (queue.any { it.priority == ActionPriority.Strong }) {
            (character as? Player)?.closeInterface()
            clearWeak()
        }
        if (queue.isEmpty()) {
            character.resumeSuspension()
        } else {
            while (queue.isNotEmpty()) {
                if (!queue.removeIf(::processed)) {
                    break
                }
            }
        }
        if (character.suspension == null) {
            behaviour = null
        }
    }

    fun contains(priority: ActionPriority): Boolean = queue.any { it.priority == priority }

    fun clearWeak() {
        queue.removeIf {
            if (it.priority == ActionPriority.Weak) {
                it.cancel()
                return@removeIf true
            }
            false
        }
    }

    private fun processed(action: Action): Boolean {
        if (action.priority.closeInterfaces) {
            (character as? Player)?.closeInterface()
        }

        if (canProcess(action) && action.process()) {
            launch(action)
            return action.removed
        }
        return false
    }

    private fun canProcess(action: Action) = action.priority == ActionPriority.Soft || (noDelay() && noInterrupt())

    private fun noDelay() = character["delay", 0] <= 0

    private fun noInterrupt() = character is NPC || (character is Player && !character.hasScreenOpen())

    private fun launch(action: Action) {
        if (character.resumeSuspension()) {
            return
        }
        launch {
            try {
                behaviour = action.behaviour
                action.action.invoke(action)
            } finally {
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

fun NPC.queue(initialDelay: Int = 0, block: suspend NPCAction.() -> Unit) {
    queue.add(NPCAction(this, ActionPriority.Normal, initialDelay, action = block))
}

fun NPC.strongQueue(initialDelay: Int = 0, block: suspend NPCAction.() -> Unit) {
    queue.add(NPCAction(this, ActionPriority.Strong, initialDelay, action = block))
}

fun NPC.softQueue(initialDelay: Int = 0, block: suspend NPCAction.() -> Unit) {
    queue.add(NPCAction(this, ActionPriority.Soft, initialDelay, action = block))
}

fun Player.queue(initialDelay: Int = 0, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, ActionPriority.Normal, initialDelay, action = block))
}

fun Player.softQueue(initialDelay: Int = 0, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, ActionPriority.Soft, initialDelay, action = block))
}

fun Player.weakQueue(initialDelay: Int = 0, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, ActionPriority.Weak, initialDelay, action = block))
}

fun Player.strongQueue(initialDelay: Int = 0, block: suspend PlayerAction.() -> Unit) {
    queue.add(PlayerAction(this, ActionPriority.Strong, initialDelay, action = block))
}