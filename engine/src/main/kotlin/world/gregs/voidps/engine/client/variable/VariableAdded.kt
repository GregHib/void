package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

/**
 * Variable with name [key] had a sub-value [value] added to it
 */
data class VariableAdded(
    val key: String,
    val value: Any
) : Event

fun variableAdded(filter: VariableAdded.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend VariableAdded.(Player) -> Unit) {
    on<VariableAdded>(filter, priority, block)
}
