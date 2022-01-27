package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

/**
 * Player wants to kick a player from their clan chat
 * @param name The display name of the player to kick
 */
data class ClanChatKick(
    val name: String
) : Instruction