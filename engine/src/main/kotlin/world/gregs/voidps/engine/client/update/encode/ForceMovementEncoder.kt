package world.gregs.voidps.engine.client.update.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.ForceMovement

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class ForceMovementEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<ForceMovement>(mask) {

    override fun encode(writer: Writer, visual: ForceMovement) {
        val (tile1, delay1, tile2, delay2, direction) = visual
        writer.apply {
            writeByte(tile1.x, if (npc) Modifier.SUBTRACT else Modifier.NONE)
            writeByte(tile1.y, if (npc) Modifier.SUBTRACT else Modifier.SUBTRACT)
            writeByte(tile2.x, if (npc) Modifier.NONE else Modifier.INVERSE)
            writeByte(tile2.y, if (npc) Modifier.SUBTRACT else Modifier.INVERSE)
            writeShort(delay1, if (npc) Modifier.NONE else Modifier.ADD, if (npc) Endian.LITTLE else Endian.LITTLE)
            writeShort(delay2, Modifier.ADD)
            writeByte(direction.ordinal / 2, if(npc) Modifier.SUBTRACT else Modifier.NONE)
        }
    }

}