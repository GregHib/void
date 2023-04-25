package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Event

/**
 * Variable with name [key] was set to [to]
 * @param from previous value
 */
data class VariableSet(
    val key: String,
    val from: Any?,
    val to: Any?
) : Event
