package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

@JvmInline
value class ExamineObject(
    val objectId: Int,
) : Instruction
