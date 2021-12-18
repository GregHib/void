package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

@JvmInline
value class ExamineObject(
    val objectId: Int
) : Instruction