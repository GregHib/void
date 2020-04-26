package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Face(var deltaX: Int = 0, var deltaY: Int = -1) : Visual

const val FACE_MASK = 0x20

fun Player.flagFace() = visuals.flag(FACE_MASK)

fun Player.getFace() = visuals.getOrPut(FACE_MASK) { Face() }

fun Player.face(deltaX: Int = 0, deltaY: Int = -1) {
    val face = getFace()
    face.deltaX = deltaX
    face.deltaY = deltaY
    flagFace()
}