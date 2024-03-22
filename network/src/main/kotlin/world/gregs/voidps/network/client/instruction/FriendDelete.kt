package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.Instruction

data class FriendDelete(
    val friendsName: String
) : Instruction