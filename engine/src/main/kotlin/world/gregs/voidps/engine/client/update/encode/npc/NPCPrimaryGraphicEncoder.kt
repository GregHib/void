package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.entity.character.update.visual.NPC_GRAPHIC_0_MASK

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class NPCPrimaryGraphicEncoder : VisualEncoder<Graphic>(NPC_GRAPHIC_0_MASK) {

    override fun encode(writer: Writer, visual: Graphic) {
        writer.apply {
            writeShortLittle(visual.id)
            writeIntInverse(visual.packedDelayHeight)
            writeByte(visual.packedRotationRefresh)
        }
    }

}