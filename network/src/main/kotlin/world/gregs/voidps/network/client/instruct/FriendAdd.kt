package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class FriendAdd(
    val friendsName: String
) : Instruction