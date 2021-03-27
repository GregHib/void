package world.gregs.voidps.network.instruct

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.Instruction

data class Command(val prefix: String, val content: String) : Instruction, Event {

    val params = content.split(",")
}