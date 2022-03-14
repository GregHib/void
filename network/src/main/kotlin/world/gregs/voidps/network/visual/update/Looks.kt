package world.gregs.voidps.network.visual.update

import world.gregs.voidps.network.visual.BodyColour
import world.gregs.voidps.network.visual.BodyPart

interface Looks {
    var male: Boolean

    fun setColour(part: BodyColour, value: Int)
    fun getColour(index: Int): Int
    fun getColour(part: BodyColour) = getColour(part.index)

    fun get(index: Int): Int

    fun setLook(part: BodyPart, value: Int)
    fun getLook(index: Int): Int
    fun getLook(part: BodyPart) = getLook(part.index)

    fun updateConnected(part: BodyPart, skip: Boolean = false): Boolean
}