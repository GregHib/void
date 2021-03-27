package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class InteractDialogue(val interfaceId: Int, val componentId: Int, val option: Int) : Instruction