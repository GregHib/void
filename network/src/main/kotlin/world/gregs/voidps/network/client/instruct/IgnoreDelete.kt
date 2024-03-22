package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class IgnoreDelete(
    val name: String
) : Instruction