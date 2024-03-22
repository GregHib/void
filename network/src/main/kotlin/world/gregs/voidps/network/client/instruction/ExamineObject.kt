package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.Instruction

@JvmInline
value class ExamineObject(
    val objectId: Int
) : Instruction