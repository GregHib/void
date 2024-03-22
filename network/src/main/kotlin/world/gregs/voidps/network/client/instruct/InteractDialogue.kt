package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class InteractDialogue(
    val interfaceId: Int,
    val componentId: Int,
    val option: Int
) : Instruction