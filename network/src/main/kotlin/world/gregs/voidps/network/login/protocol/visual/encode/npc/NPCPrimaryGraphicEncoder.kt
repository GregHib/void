package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_GRAPHIC_1_MASK

class NPCPrimaryGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_1_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.graphics[0]
        writer.apply {
            ip2(visual.id)
            writeIntInverseMiddle(visual.packedDelayHeight)
            p1Alt2(visual.packedRotationRefresh)
        }
    }

}