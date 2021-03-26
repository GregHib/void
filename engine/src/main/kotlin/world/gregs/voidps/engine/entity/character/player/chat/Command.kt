package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.Instruction

data class Command(val prefix: String, val content: String) : Instruction, Event