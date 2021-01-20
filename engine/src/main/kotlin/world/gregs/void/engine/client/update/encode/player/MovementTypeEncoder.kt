package world.gregs.void.engine.client.update.encode.player

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.player.MOVEMENT_TYPE_MASK
import world.gregs.void.engine.entity.character.update.visual.player.MovementType

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class MovementTypeEncoder : VisualEncoder<MovementType>(MOVEMENT_TYPE_MASK) {

    override fun encode(writer: Writer, visual: MovementType) {
        writer.writeByte(visual.type.id, Modifier.INVERSE)
    }

}