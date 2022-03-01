package world.gregs.voidps.network.visual.update

import world.gregs.voidps.network.visual.BodyPart

interface Looks {
    val looks: IntArray
    fun get(index: Int): Int
    fun updateConnected(part: BodyPart): Boolean
}