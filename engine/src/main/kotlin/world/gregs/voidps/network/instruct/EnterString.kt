package world.gregs.voidps.network.instruct

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.Instruction

data class EnterString(val value: String) : Instruction, Event