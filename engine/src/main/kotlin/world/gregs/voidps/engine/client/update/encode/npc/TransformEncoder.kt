package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.Transformation

class TransformEncoder : VisualEncoder(TRANSFORM_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: Visuals) {
        val visual = visuals.aspects[mask] as Transformation
        writer.writeShortAdd(visual.id)
    }

}