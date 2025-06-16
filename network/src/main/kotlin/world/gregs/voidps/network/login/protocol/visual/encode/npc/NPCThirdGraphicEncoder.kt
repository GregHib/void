package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_GRAPHIC_3_MASK

class NPCThirdGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_3_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.thirdGraphic
        writer.apply {
            writeShort(visual.id)
            writeInt(visual.packedDelayHeight)
            writeByteAdd(visual.packedRotationRefresh)
        }
    }

}