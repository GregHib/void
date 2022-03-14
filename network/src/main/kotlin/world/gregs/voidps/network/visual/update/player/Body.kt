package world.gregs.voidps.network.visual.update.player

import world.gregs.voidps.network.visual.BodyColour
import world.gregs.voidps.network.visual.BodyPart

interface Body {
    var male: Boolean

    fun setColour(part: BodyColour, value: Int)
    fun getColour(part: BodyColour) = getColour(part.index)
    fun getColour(index: Int): Int

    fun get(index: Int): Int

    fun setLook(part: BodyPart, value: Int)
    fun getLook(part: BodyPart) = getLook(part.index)
    fun getLook(index: Int): Int

    fun updateConnected(part: BodyPart, skip: Boolean = false): Boolean
}