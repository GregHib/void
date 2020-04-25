package rs.dusk.engine.client.update.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.Graphics
import rs.dusk.utility.func.toInt

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class GraphicsEncoder(private val npc: Boolean, private val index: Int) : VisualEncoder<Graphics>(Graphics::class) {

    override fun encode(writer: Writer, visual: Graphics) {
        val (id, delay, height, rotation, forceRefresh) = visual.graphics.getOrNull(index) ?: return
        writer.apply {
            val trajectory = (delay and 0xffff) or (height shl 16)
            val slot = 0
            val details = (rotation and 0x7) or (slot shl 3) or (forceRefresh.toInt() shl 7)
            writeShort(
                id,
                if (npc && index == 2 || !npc && index != 1) Modifier.ADD else Modifier.NONE,
                if (npc && index == 1 || !npc && index != 1) Endian.LITTLE else Endian.BIG
            )
            writeInt(
                trajectory,
                if (npc && index == 1 || !npc && index != 1) Modifier.INVERSE else Modifier.NONE,
                if (npc && index == 1 || !npc && index != 1) Endian.MIDDLE
                else if (npc) Endian.BIG else Endian.LITTLE
            )
            writeByte(
                details,
                if (npc && index == 1 || !npc && index != 1) Modifier.INVERSE
                else if (npc && index == 3 || !npc) Modifier.ADD else Modifier.NONE
            )
        }
    }

}