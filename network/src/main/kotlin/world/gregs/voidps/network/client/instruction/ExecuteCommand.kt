package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class ExecuteCommand(val command: String, val automatic: Boolean, val tab: Boolean) : Instruction
