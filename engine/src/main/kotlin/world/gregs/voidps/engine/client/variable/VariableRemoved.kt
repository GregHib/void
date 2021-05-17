package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Event

data class VariableRemoved(val key: String, val value: Any, val before: Int, val after: Int) : Event
