package world.gregs.void.engine.client.update.encode.player

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.void.engine.entity.character.update.visual.player.TemporaryMoveType

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class TemporaryMoveTypeEncoder : VisualEncoder<TemporaryMoveType>(TEMPORARY_MOVE_TYPE_MASK) {

    override fun encode(writer: Writer, visual: TemporaryMoveType) {
        writer.writeByte(visual.type.id, Modifier.SUBTRACT)
    }

}