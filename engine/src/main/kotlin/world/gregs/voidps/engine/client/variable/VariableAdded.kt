package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Event

/**
 * Variable with name [key] had a sub-value [value] added to it
 * @param before variable value before addition
 * @param after variable value after addition
 */
data class VariableAdded(
    val key: String,
    val value: Any,
    val before: Int,
    val after: Int
) : Event
