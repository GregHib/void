package world.gregs.voidps.engine.queue

import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.closeInterface
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.suspend.EventSuspension
import java.util.*

class ActionQueue(private val character: Character) : CoroutineScope {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            throwable.printStackTrace()
        }
    }
    var suspend: EventSuspension? = null
        set(value) {
            suspended = value != null
            field = value
        }

    var suspended = false
        private set

    override val coroutineContext = Dispatchers.Unconfined + errorHandler
    private val queue = LinkedList<QueuedAction>()

    fun tick() {
        if (queue.any { it.priority == ActionPriority.Strong }) {
            (character as? Player)?.closeInterface()
            clearWeak()
        }
        while (queue.isNotEmpty()) {
            if (!queue.removeIf(::processed)) {
                break
            }
        }
    }

    fun clearWeak() {
        queue.removeIf {
            if (it.priority == ActionPriority.Weak) {
                it.cancel()
                return@removeIf true
            }
            false
        }
    }

    private fun processed(action: QueuedAction): Boolean {
        if (action.priority.closeInterfaces) {
            (character as? Player)?.closeInterface()
        }

        if (canProcess(action) && (character is NPC || (character is Player && !character.hasScreenOpen())) && action.process()) {
            launch(action)
            return action.removed
        }
        return false
    }

    private fun launch(action: QueuedAction) {
        val suspend = suspend
        if (suspend != null) {
            if (suspend.ready()) {
                suspend.resume()
            }
            if (suspend.finished()) {
                this.suspend = null
            }
            return
        }
        launch {
            try {
                action.action.invoke(action)
            } finally {
                action.cancel()
            }
        }
    }

    private fun canProcess(action: QueuedAction) = action.priority == ActionPriority.Soft || character["delay", 0] <= 0

    fun add(action: QueuedAction) {
        queue.add(action)
    }

}

fun NPC.queue(initialDelay: Int = 0, block: suspend NPCQueuedAction.() -> Unit) {
    queue.add(NPCQueuedAction(this, ActionPriority.Normal, initialDelay, action = block))
}

fun NPC.strongQueue(initialDelay: Int = 0, block: suspend NPCQueuedAction.() -> Unit) {
    queue.add(NPCQueuedAction(this, ActionPriority.Strong, initialDelay, action = block))
}

fun Player.queue(initialDelay: Int = 0, block: suspend PlayerQueuedAction.() -> Unit) {
    queue.add(PlayerQueuedAction(this, ActionPriority.Normal, initialDelay, action = block))
}

fun Player.softQueue(initialDelay: Int = 0, block: suspend PlayerQueuedAction.() -> Unit) {
    queue.add(PlayerQueuedAction(this, ActionPriority.Soft, initialDelay, action = block))
}

fun Player.weakQueue(initialDelay: Int = 0, block: suspend PlayerQueuedAction.() -> Unit) {
    queue.add(PlayerQueuedAction(this, ActionPriority.Weak, initialDelay, action = block))
}

fun Player.strongQueue(initialDelay: Int = 0, block: suspend PlayerQueuedAction.() -> Unit) {
    queue.add(PlayerQueuedAction(this, ActionPriority.Strong, initialDelay, action = block))
}
