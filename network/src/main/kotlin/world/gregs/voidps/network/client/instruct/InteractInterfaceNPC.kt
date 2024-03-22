package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class InteractInterfaceNPC(
    val npcIndex: Int,
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int
) : Instruction