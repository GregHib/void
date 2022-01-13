package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class PublicChat(
    val message: String,
    val effects: Int
) : Instruction