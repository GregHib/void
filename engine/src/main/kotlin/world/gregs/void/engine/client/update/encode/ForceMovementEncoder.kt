package world.gregs.void.engine.client.update.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.ForceMovement

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class ForceMovementEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<ForceMovement>(mask) {

    override fun encode(writer: Writer, visual: ForceMovement) {
        val (tile1, delay1, tile2, delay2, direction) = visual
        writer.apply {
            writeByte(tile1.x, Modifier.SUBTRACT)
            writeByte(tile1.y, if (npc) Modifier.SUBTRACT else Modifier.NONE)
            writeByte(tile2.x, if (npc) Modifier.INVERSE else Modifier.ADD)
            writeByte(tile2.y, if (npc) Modifier.INVERSE else Modifier.ADD)
            writeShort(delay1, order = if (npc) Endian.BIG else Endian.LITTLE)
            writeShort(delay2, Modifier.ADD, Endian.LITTLE)
            writeByte(direction.ordinal / 2, if (npc) Modifier.SUBTRACT else Modifier.ADD)
        }
    }

}