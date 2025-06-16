package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_GRAPHIC_4_MASK

class NPCFourthGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_4_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.fourthGraphic
        writer.apply {
            writeShortAdd(visual.id)
            writeInt(visual.packedDelayHeight)
            writeByte(visual.packedRotationRefresh)
        }
    }

}