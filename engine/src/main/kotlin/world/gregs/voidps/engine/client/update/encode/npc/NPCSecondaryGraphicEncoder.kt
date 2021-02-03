package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
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
            writeShort(visual.id, order = Endian.LITTLE)
            writeInt(visual.packedDelayHeight, order = Endian.LITTLE)
            writeByte(visual.packedRotationRefresh, Modifier.SUBTRACT)
        }
    }

}