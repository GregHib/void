package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractDialogue(
    val interfaceId: Int,
    val componentId: Int,
    val option: Int,
) : Instruction
