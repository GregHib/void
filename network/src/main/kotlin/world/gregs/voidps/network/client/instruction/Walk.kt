package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class Walk(val x: Int, val y: Int, val walk: Boolean = false, val minimap: Boolean = false) : Instruction
