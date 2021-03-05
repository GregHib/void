package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.entity.character.update.visual.NPC_GRAPHIC_1_MASK

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class NPCSecondaryGraphicEncoder : VisualEncoder<Graphic>(NPC_GRAPHIC_1_MASK) {

    override fun encode(writer: Writer, visual: Graphic) {
        writer.apply {
            writeShortLittle(visual.id)
            writeIntLittle(visual.packedDelayHeight)
            writeByteSubtract(visual.packedRotationRefresh)
        }
    }

}