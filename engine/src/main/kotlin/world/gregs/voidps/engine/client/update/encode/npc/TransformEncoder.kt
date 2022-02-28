package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.NPCVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.TRANSFORM_MASK

class TransformEncoder : VisualEncoder<NPCVisuals>(TRANSFORM_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        writer.writeShortAdd(visuals.transform.id)
    }

}