package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractInterfaceNPC(
    val npcIndex: Int,
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int,
) : Instruction
