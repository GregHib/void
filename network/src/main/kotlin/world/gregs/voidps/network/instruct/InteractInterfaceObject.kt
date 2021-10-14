package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class InteractInterfaceObject(
    val objectId: Int,
    val x: Int,
    val y: Int,
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int
) : Instruction