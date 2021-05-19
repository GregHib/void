package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_GRAPHIC_1_MASK

class PlayerSecondaryGraphicEncoder : VisualEncoder<Graphic>(PLAYER_GRAPHIC_1_MASK) {

    override fun encode(writer: Writer, visual: Graphic) {
        writer.apply {
            writeShortAddLittle(visual.id)
            writeIntLittle(visual.packedDelayHeight)
            writeByteSubtract(visual.packedRotationRefresh)
        }
    }

}