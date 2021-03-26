package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class InteractFloorItem(val id: Int, val x: Int, val y: Int, val option: Int) : Instruction