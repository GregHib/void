package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_GRAPHIC_0_MASK
import world.gregs.voidps.utility.func.toInt

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class PlayerPrimaryGraphicEncoder : VisualEncoder<Graphic>(PLAYER_GRAPHIC_0_MASK) {

    override fun encode(writer: Writer, visual: Graphic) {
        writer.apply {
            writeShort(visual.id, Modifier.ADD, Endian.LITTLE)
            writeInt(visual.packedDelayHeight, Modifier.INVERSE, Endian.MIDDLE)
            writeByte(visual.packedRotationRefresh, Modifier.ADD)
        }
    }

}