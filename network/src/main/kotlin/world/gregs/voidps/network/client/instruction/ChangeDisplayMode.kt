package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class ChangeDisplayMode(
    val displayMode: Int,
    val width: Int,
    val height: Int,
    val antialiasLevel: Int,
) : Instruction
