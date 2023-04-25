package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class InteractInterfaceFloorItem(
    val floorItem: Int,
    val x: Int,
    val y: Int,
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int
) : Instruction