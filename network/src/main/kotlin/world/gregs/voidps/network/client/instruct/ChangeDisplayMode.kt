package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class ChangeDisplayMode(
    val displayMode: Int,
    val width: Int,
    val height: Int,
    val antialiasLevel: Int
) : Instruction