package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * Variable with name [key] had a sub-value [value] removed from it
 */
data class VariableRemoved(
    val key: String,
    val value: Any
) : Event

fun variableRemoved(vararg variables: String, block: suspend VariableRemoved.(Player) -> Unit) {
    for (variable in variables) {
        on<VariableRemoved>({ wildcardEquals(variable, key) }, block = block)
    }
}
