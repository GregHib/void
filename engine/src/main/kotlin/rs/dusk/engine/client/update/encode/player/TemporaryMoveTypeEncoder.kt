package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.player.TEMPORARY_MOVE_TYPE_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.TemporaryMoveType

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class TemporaryMoveTypeEncoder : VisualEncoder<TemporaryMoveType>(TEMPORARY_MOVE_TYPE_MASK) {

    override fun encode(writer: Writer, visual: TemporaryMoveType) {
        writer.writeByte(visual.type, Modifier.SUBTRACT)
    }

}