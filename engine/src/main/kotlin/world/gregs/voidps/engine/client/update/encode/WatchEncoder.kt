package world.gregs.voidps.engine.client.update.encode

import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Watch
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer

/**
 * @author GregHib <greg@gregs.world>
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