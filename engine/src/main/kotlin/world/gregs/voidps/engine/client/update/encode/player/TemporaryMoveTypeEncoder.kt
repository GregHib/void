package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class TemporaryMoveTypeEncoder : VisualEncoder<TemporaryMoveType>(TEMPORARY_MOVE_TYPE_MASK) {

    override fun encode(writer: Writer, visual: TemporaryMoveType) {
        writer.writeByteSubtract(visual.type.id)
    }

}