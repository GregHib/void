package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

@JvmInline
value class ExamineNpc(
    val npcIndex: Int
) : Instruction