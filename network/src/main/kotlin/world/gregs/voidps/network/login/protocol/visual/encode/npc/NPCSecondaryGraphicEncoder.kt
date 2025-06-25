package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_GRAPHIC_2_MASK

class NPCSecondaryGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_2_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.graphics[1]
        writer.apply {
            p2Alt2(visual.id)
            writeInt(visual.packedDelayHeight)
            writeByte(visual.packedRotationRefresh)
        }
    }

}