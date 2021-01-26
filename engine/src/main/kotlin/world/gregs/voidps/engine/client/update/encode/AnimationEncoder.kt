package world.gregs.voidps.engine.client.update.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Animation

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class AnimationEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<Animation>(mask) {

    override fun encode(writer: Writer, visual: Animation) {
        val (first, second, third, fourth, speed) = visual
        writer.apply {
            writeShort(first, Modifier.ADD)
            writeByte(speed, if (npc) Modifier.NONE else Modifier.ADD)
        }
    }
}