package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class ExecuteCommand(val prefix: String, val content: String) : Instruction
