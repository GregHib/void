package rs.dusk.engine.client.update.encode

import rs.dusk.core.io.write.Writer
import rs.dusk.engine.model.entity.character.update.VisualEncoder
import rs.dusk.engine.model.entity.character.update.visual.ForceChat

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