package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.NPC_GRAPHIC_1_MASK

class NPCPrimaryGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_1_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.primaryGraphic
        writer.apply {
            writeShortLittle(visual.id)
            writeIntMiddle(visual.packedDelayHeight)
            writeByte(visual.packedRotationRefresh)
        }
    }

}