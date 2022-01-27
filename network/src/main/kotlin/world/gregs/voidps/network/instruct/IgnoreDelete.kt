package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class IgnoreDelete(
    val name: String
) : Instruction