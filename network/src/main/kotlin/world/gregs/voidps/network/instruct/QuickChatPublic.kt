package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class QuickChatPublic(
    val script: Int,
    val file: Int,
    val data: ByteArray
) : Instruction