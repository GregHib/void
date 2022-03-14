package world.gregs.voidps.network.visual.update

import world.gregs.voidps.network.visual.BodyPart

interface Looks {
    val looks: IntArray
    var male: Boolean
    fun get(index: Int): Int
    fun updateConnected(part: BodyPart, skip: Boolean = false): Boolean
}