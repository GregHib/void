package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class ChatPrivate(
    val friend: String,
    val message: String
) : Instruction