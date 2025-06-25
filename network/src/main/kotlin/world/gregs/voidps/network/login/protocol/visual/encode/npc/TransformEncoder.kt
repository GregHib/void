package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.TRANSFORM_MASK

class TransformEncoder : VisualEncoder<NPCVisuals>(TRANSFORM_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        writer.ip2(visuals.transform.id)
    }

}