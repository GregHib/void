package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class InteractPlayer(
    val playerIndex: Int,
    val option: Int
) : Instruction