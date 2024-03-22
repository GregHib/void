package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

@JvmInline
value class ExamineNpc(
    val npcId: Int
) : Instruction