package world.gregs.voidps.engine.queue

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.entity.character.CharacterContext

abstract class Action(
    val name: String,
    val priority: ActionPriority,
    delay: Int = 0,
    val behaviour: LogoutBehaviour = LogoutBehaviour.Discard,
    val action: suspend Action.() -> Unit = {}
) : CharacterContext {

    var delay: Int = delay
        private set

    var removed = false
        private set

    /**
     * Executes action once delay has reached zero
     * @return if action was executed this call
     */
    fun process(): Boolean {
        return !removed && this.delay-- <= 0
    }

    fun cancel(invoke: Boolean = true) {
        delay = -1
        removed = true
        if (invoke) {
            onCancel?.invoke()
            onCancel = null
        }
    }

    override fun toString(): String {
        return "${name}_${priority.name.toSnakeCase()}_${behaviour.name.toSnakeCase()}"
    }
}