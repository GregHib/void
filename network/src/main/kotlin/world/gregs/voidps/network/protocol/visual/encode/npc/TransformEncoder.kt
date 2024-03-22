package world.gregs.voidps.network.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.protocol.visual.NPCVisuals
import world.gregs.voidps.network.protocol.visual.VisualEncoder
import world.gregs.voidps.network.protocol.visual.VisualMask.TRANSFORM_MASK

class TransformEncoder : VisualEncoder<NPCVisuals>(TRANSFORM_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        writer.writeShortAdd(visuals.transform.id)
    }

}