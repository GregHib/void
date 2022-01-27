package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class ClanChatRank(
    val name: String,
    val rank: Int
) : Instruction