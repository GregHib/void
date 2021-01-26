package world.gregs.voidps.engine.client.update.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.TimeBar
import world.gregs.voidps.utility.func.toInt

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class TimeBarEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<TimeBar>(mask) {

    override fun encode(writer: Writer, visual: TimeBar) {
        val (full, exponentialDelay, delay, increment) = visual
        writer.apply {
            writeShort((full.toInt() * 0x8000) or (exponentialDelay and 0x7fff), order = Endian.LITTLE)
            writeByte(delay, if (npc) Modifier.INVERSE else Modifier.SUBTRACT)
            writeByte(increment, Modifier.INVERSE)
        }
    }

}