package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class InteractObject(
    val objectId: Int,
    val x: Int,
    val y: Int,
    val option: Int
) : Instruction