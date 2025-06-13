package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

/**
 * A freeform [text] a player wants (but has yet) to say to everyone nearby.
 */
data class ChatPublic(
    val text: String,
    val effects: Int,
) : Instruction
