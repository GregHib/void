package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.player.Face

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class FaceEncoder : VisualEncoder<Face>(Face::class) {

    override fun encode(writer: Writer, visual: Face) {
        writer.writeShort(visual.direction)
    }

}