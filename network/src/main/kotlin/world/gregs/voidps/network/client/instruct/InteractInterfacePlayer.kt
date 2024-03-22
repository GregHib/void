package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class InteractInterfacePlayer(
    val playerIndex: Int,
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int
) : Instruction