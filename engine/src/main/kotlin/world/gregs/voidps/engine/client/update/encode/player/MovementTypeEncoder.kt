package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.player.MOVEMENT_TYPE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.MovementType

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class MovementTypeEncoder : VisualEncoder<MovementType>(MOVEMENT_TYPE_MASK) {

    override fun encode(writer: Writer, visual: MovementType) {
        writer.writeByte(visual.type.id, Modifier.INVERSE)
    }

}