package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractInterface(
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val slotId: Int,
    val option: Int
) : Instruction