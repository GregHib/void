package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

/**
 * Variable with name [key] had a sub-value [value] removed from it
 */
data class VariableRemoved(
    val key: String,
    val value: Any
) : Event

fun variableRemoved(filter: VariableRemoved.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend VariableRemoved.(Player) -> Unit) {
    on<VariableRemoved>(filter, priority, block)
}
