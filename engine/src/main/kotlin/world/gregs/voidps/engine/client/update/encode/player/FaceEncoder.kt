package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.player.FACE_DIRECTION_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.Face
import kotlin.math.atan2

class FaceEncoder : VisualEncoder<Face>(FACE_DIRECTION_MASK, initial = true) {

    override fun encode(writer: Writer, visual: Face) {
        writer.writeShort(getFaceDirection(visual.deltaX, visual.deltaY))
    }

    companion object {
        fun getFaceDirection(xOffset: Int, yOffset: Int): Int {
            return (atan2(xOffset * -1.0, yOffset * -1.0) * 2607.5945876176133).toInt() and 0x3fff
        }
    }

}