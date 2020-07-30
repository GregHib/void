package rs.dusk.engine.client.update.encode.npc

import rs.dusk.core.io.Endian
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.character.update.VisualEncoder
import rs.dusk.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import rs.dusk.engine.entity.character.update.visual.npc.Transform

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class TransformEncoder : VisualEncoder<Transform>(TRANSFORM_MASK) {

    override fun encode(writer: Writer, visual: Transform) {
        writer.writeShort(visual.id, order = Endian.LITTLE)
    }

}