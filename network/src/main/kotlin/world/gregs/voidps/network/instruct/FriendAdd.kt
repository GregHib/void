package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class FriendAdd(
    val friendsName: String
) : Instruction