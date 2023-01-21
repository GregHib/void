package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.event.suspend.EventSuspension

abstract class QueuedAction(
    val priority: ActionPriority,
    delay: Int = 0,
    val behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    val action: suspend QueuedAction.() -> Unit = {}
) {

    var suspend: EventSuspension? = null

    var delay: Int = delay
        private set

    var removed = false
        private set

    /**
     * Executes action once delay has reached zero
     * @return if action was executed this call
     */
    fun process(): Boolean {
        return --this.delay <= 0
    }

    fun cancel() {
        delay = -1
        removed = true
    }

    fun stop() {
        cancel()
    }

    override fun toString(): String {
        return "${priority.name}_${behaviour.name}_${super.toString()}"
    }
}