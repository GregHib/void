package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.Transformation

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class TransformEncoder : VisualEncoder<Transformation>(TRANSFORM_MASK) {

    override fun encode(writer: Writer, visual: Transformation) {
        writer.writeShort(visual.id, Modifier.ADD)
    }

}