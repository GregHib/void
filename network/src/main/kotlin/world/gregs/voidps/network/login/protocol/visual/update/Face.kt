package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual
import kotlin.math.atan2

/**
 * Turn a character to face [targetX], [targetY] or [direction]
 */
data class Face(
    var targetX: Int = 0,
    var targetY: Int = 0,
    var direction: Int = 0,
) : Visual {

    override fun reset() {
        targetX = 0
        targetY = 0
        direction = 0
    }

    companion object {
        fun getFaceDirection(xOffset: Int, yOffset: Int): Int = (atan2(xOffset * -1.0, yOffset * -1.0) * 2607.5945876176133).toInt() and 0x3fff
    }
}
