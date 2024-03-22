package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

data class InteractNPC(
    val npcIndex: Int,
    val option: Int,
) : Instruction