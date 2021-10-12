package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event

data class Command(val prefix: String, val content: String) : Event