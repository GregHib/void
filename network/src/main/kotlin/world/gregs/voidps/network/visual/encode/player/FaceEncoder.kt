package world.gregs.voidps.network.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.FACE_DIRECTION_MASK
import kotlin.math.atan2

class FaceEncoder : VisualEncoder<PlayerVisuals>(FACE_DIRECTION_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val visual = visuals.face
        writer.writeShort(getFaceDirection(visual.deltaX, visual.deltaY))
    }

    companion object {
        fun getFaceDirection(xOffset: Int, yOffset: Int): Int {
            return (atan2(xOffset * -1.0, yOffset * -1.0) * 2607.5945876176133).toInt() and 0x3fff
        }
    }

}