package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class MoveContainerItem(
    val fromId: Int,
    val fromComponentId: Int,
    val fromType: Int,
    val fromSlot: Int,
    val toId: Int,
    val toComponentId: Int,
    val toType: Int,
    val toSlot: Int
) : Instruction