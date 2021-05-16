package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.event.Event

data class VariableSet(val key: String, val from: Any, val to: Any) : Event
