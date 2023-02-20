package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Event

/**
 * Variable with name [key] had a sub-value [value] added to it
 */
data class VariableAdded(
    val key: String,
    val value: Any
) : Event
