package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class PrivateQuickChat(
    val name: String,
    val file: Int,
    val data: ByteArray
) : Instruction