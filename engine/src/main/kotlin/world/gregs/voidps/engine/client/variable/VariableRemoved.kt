package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Event

/**
 * Variable with name [key] had a sub-value [value] removed from it
 */
data class VariableRemoved(
    val key: String,
    val value: Any
) : Event
