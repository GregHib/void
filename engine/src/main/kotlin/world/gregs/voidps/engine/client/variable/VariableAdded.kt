package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * Variable with name [key] had a sub-value [value] added to it
 */
data class VariableAdded(
    val key: String,
    val value: Any
) : Event

fun variableAdded(vararg variables: String, block: suspend VariableAdded.(Player) -> Unit) {
    for (variable in variables) {
        on<VariableAdded>({ wildcardEquals(variable, key) }, block = block)
    }
}
