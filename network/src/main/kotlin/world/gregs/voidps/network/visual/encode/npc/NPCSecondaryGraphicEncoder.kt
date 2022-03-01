package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.NPC_GRAPHIC_2_MASK

class NPCSecondaryGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_2_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.secondaryGraphic
        writer.apply {
            writeShortLittle(visual.id)
            writeIntLittle(visual.packedDelayHeight)
            writeByteSubtract(visual.packedRotationRefresh)
        }
    }

}