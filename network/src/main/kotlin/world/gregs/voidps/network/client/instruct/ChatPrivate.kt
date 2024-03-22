package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class ChatPrivate(
    val friend: String,
    val message: String
) : Instruction