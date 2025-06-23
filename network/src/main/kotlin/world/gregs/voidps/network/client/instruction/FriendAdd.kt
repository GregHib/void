package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class FriendAdd(
    val friendsName: String,
) : Instruction
