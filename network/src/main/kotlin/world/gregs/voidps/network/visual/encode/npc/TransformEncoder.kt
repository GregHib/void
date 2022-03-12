package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.TRANSFORM_MASK

class TransformEncoder : VisualEncoder<NPCVisuals>(TRANSFORM_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        writer.writeShortAdd(visuals.transform.id)
    }

}