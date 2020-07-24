package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.model.entity.character.update.VisualEncoder
import rs.dusk.engine.model.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK
import rs.dusk.engine.model.entity.character.update.visual.player.TemporaryMoveType

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class TemporaryMoveTypeEncoder : VisualEncoder<TemporaryMoveType>(TEMPORARY_MOVE_TYPE_MASK) {

    override fun encode(writer: Writer, visual: TemporaryMoveType) {
        writer.writeByte(visual.type.id, Modifier.SUBTRACT)
    }

}