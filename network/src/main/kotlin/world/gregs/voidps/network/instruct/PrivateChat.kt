package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class PrivateChat(
    val friend: String,
    val message: String
) : Instruction