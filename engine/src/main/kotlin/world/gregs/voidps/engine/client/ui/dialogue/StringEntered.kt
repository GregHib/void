package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.Instruction

data class StringEntered(val value: String) : Instruction, Event