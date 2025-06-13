package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class ClanChatRank(
    val name: String,
    val rank: Int,
) : Instruction
