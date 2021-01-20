package world.gregs.void.engine.client.update.encode.npc

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import world.gregs.void.engine.entity.character.update.visual.npc.Transformation

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class TransformEncoder : VisualEncoder<Transformation>(TRANSFORM_MASK) {

    override fun encode(writer: Writer, visual: Transformation) {
        writer.writeShort(visual.id, order = Endian.LITTLE)
    }

}