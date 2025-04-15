package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

/**
 * A freeform [message] a player wants (but has yet) to send directly to a [friend].
 */
data class ChatPrivate(
    val friend: String,
    val message: String
) : Instruction