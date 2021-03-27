package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class InteractInterface(
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int,
    val option: Int
) : Instruction