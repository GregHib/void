package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.character.update.VisualEncoder
import rs.dusk.engine.entity.character.update.visual.player.FACE_DIRECTION_MASK
import rs.dusk.engine.entity.character.update.visual.player.Face
import kotlin.math.atan2

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class FaceEncoder : VisualEncoder<Face>(FACE_DIRECTION_MASK) {

    override fun encode(writer: Writer, visual: Face) {
        writer.writeShort(getFaceDirection(visual.deltaX, visual.deltaY))
    }

    companion object {
        fun getFaceDirection(xOffset: Int, yOffset: Int): Int {
            return (atan2(xOffset * -1.0, yOffset * -1.0) * 2607.5945876176133).toInt() and 0x3fff
        }
    }

}