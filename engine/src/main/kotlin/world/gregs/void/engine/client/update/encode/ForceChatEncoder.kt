package world.gregs.void.engine.client.update.encode

import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.ForceChat

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class ForceChatEncoder(mask: Int) : VisualEncoder<ForceChat>(mask) {

    override fun encode(writer: Writer, visual: ForceChat) {
        val (text) = visual
        writer.apply {
            writeString(text)
        }
    }

}