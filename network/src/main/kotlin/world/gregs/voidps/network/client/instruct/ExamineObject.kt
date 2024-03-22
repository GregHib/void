package world.gregs.voidps.network.client.instruct

import world.gregs.voidps.network.Instruction

@JvmInline
value class ExamineObject(
    val objectId: Int
) : Instruction