package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.ForceMovement
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_FORCE_MOVEMENT_MASK

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class PlayerForceMovementEncoder : VisualEncoder<ForceMovement>(PLAYER_FORCE_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visual: ForceMovement) {
        val (tile1, delay1, tile2, delay2, direction) = visual
        writer.apply {
            writeByte(tile1.x)
            writeByte(tile1.y, Modifier.SUBTRACT)
            writeByte(tile2.x, Modifier.INVERSE)
            writeByte(tile2.y, Modifier.INVERSE)
            writeShort(delay1, Modifier.ADD, Endian.LITTLE)
            writeShort(delay2, Modifier.ADD)
            writeByte(direction.ordinal / 2)
        }
    }

}