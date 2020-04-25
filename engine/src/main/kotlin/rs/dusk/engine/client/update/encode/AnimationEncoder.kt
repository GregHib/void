package rs.dusk.engine.client.update.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.Animation

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class AnimationEncoder(private val npc: Boolean) : VisualEncoder<Animation>(Animation::class) {

    override fun encode(writer: Writer, visual: Animation) {
        val (first, second, third, fourth, speed) = visual
        writer.apply {
            val order = if (npc) Endian.BIG else Endian.LITTLE
            writeShort(first, order = order)
            writeShort(second, order = order)
            writeShort(third, order = order)
            writeShort(fourth, order = order)
            writeByte(speed, if (npc) Modifier.NONE else Modifier.ADD)
        }
    }
}