package world.gregs.voidps.network.instruct

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.Instruction

data class EnterInt(val value: Int) : Instruction, Event