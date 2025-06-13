package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractObject(
    val objectId: Int,
    val x: Int,
    val y: Int,
    val option: Int,
) : Instruction
