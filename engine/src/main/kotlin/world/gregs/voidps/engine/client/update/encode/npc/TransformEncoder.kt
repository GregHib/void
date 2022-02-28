package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.Transformation

class TransformEncoder : VisualEncoder<Transformation>(TRANSFORM_MASK, initial = true) {

    override fun encode(writer: Writer, visual: Transformation) {
        writer.writeShortAdd(visual.id)
    }

}