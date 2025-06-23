package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractFloorItem(
    val id: Int,
    val x: Int,
    val y: Int,
    val option: Int,
) : Instruction
