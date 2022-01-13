package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

/**
 * Player wants to join a friends chat
 * @param name The display name of the friend whose chat to join
 */
data class ClanChatJoin(
    val name: String
) : Instruction