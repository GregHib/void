package world.gregs.void.engine.client.update.encode

import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.Watch
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.Writer

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class WatchEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<Watch>(mask) {

    override fun encode(writer: Writer, visual: Watch) {
        writer.writeShort(
            visual.index,
            if (npc) Modifier.NONE else Modifier.ADD,
            if (npc) Endian.LITTLE else Endian.BIG
        )
    }

}