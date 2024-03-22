package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.Instruction

data class ChatPublic(
    val message: String,
    val effects: Int
) : Instruction