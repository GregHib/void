package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class ChatPublic(
    val message: String,
    val effects: Int
) : Instruction