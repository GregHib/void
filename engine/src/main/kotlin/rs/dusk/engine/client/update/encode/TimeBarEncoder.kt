package rs.dusk.engine.client.update.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.character.update.VisualEncoder
import rs.dusk.engine.entity.character.update.visual.TimeBar
import rs.dusk.utility.func.toInt

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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