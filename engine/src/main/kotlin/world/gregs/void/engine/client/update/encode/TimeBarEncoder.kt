package world.gregs.void.engine.client.update.encode

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.TimeBar
import world.gregs.void.utility.func.toInt

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class TimeBarEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<TimeBar>(mask) {

    override fun encode(writer: Writer, visual: TimeBar) {
        val (full, exponentialDelay, delay, increment) = visual
        writer.apply {
            writeShort((full.toInt() * 0x8000) or (exponentialDelay and 0x7fff))
            writeByte(delay, if (npc) Modifier.INVERSE else Modifier.NONE)
            writeByte(increment, Modifier.INVERSE)
        }
    }

}