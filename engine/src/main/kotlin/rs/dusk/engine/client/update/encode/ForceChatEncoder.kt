package rs.dusk.engine.client.update.encode

import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.ForceChat

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