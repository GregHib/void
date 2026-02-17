package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractInterfaceItem(
    val fromItem: Int,
    val toItem: Int,
    val fromSlot: Int,
    val toSlot: Int,
    val fromInterfaceId: Int,
    val fromComponentId: Int,
    val toInterfaceId: Int,
    val toComponentId: Int,
) : Instruction
