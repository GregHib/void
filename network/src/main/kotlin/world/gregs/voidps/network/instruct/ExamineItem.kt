package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

@JvmInline
value class ExamineItem(
    val itemId: Int
) : Instruction