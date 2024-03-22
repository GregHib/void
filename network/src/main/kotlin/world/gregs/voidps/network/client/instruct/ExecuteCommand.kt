package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class ExecuteCommand(val prefix: String, val content: String) : Instruction