package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class ExecuteCommand(val prefix: String, val content: String) : Instruction