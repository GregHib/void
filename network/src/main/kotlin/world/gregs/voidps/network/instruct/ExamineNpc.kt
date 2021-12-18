package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

@JvmInline
value class ExamineNpc(
    val npcId: Int
) : Instruction