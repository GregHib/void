package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.NPCVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.NPC_GRAPHIC_1_MASK

class NPCSecondaryGraphicEncoder : VisualEncoder<NPCVisuals>(NPC_GRAPHIC_1_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.secondaryGraphic
        writer.apply {
            writeShortLittle(visual.id)
            writeIntLittle(visual.packedDelayHeight)
            writeByteSubtract(visual.packedRotationRefresh)
        }
    }

}