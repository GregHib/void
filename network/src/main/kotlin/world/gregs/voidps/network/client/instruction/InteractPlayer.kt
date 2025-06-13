package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractPlayer(
    val playerIndex: Int,
    val option: Int,
) : Instruction
