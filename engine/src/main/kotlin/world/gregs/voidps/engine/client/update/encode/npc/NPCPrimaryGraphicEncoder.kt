package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.NPCVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.NPC_GRAPHIC_0_MASK

class NPCPrimaryGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_0_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.primaryGraphic
        writer.apply {
            writeShortLittle(visual.id)
            writeIntMiddle(visual.packedDelayHeight)
            writeByte(visual.packedRotationRefresh)
        }
    }

}