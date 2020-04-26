package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.player.MOVEMENT_SPEED_MASK
import rs.dusk.engine.entity.model.visual.visuals.player.MovementSpeed

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class MovementSpeedEncoder : VisualEncoder<MovementSpeed>(MOVEMENT_SPEED_MASK) {

    override fun encode(writer: Writer, visual: MovementSpeed) {
        writer.writeByte(visual.running, Modifier.SUBTRACT)
    }

}