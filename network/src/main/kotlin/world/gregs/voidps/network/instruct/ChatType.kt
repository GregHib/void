package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

/**
 * Notified the type of message before a message is sent
 * @param type The type of message sent (0 = public, 1 = friends chat)
 */
data class ChatType(
    val type: Int
) : Instruction