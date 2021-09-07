package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Event

/**
 * Variable with name [key] had a sub-value [value] removed from it
 * @param before variable value before subtraction
 * @param after variable value after subtraction
 */
data class VariableRemoved(
    val key: String,
    val value: Any,
    val before: Int,
    val after: Int
) : Event
