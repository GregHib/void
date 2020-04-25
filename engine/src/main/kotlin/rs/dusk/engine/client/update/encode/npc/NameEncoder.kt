package rs.dusk.engine.client.update.encode.npc

import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.npc.Name

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class NameEncoder : VisualEncoder<Name>(Name::class) {

    override fun encode(writer: Writer, visual: Name) {
        writer.writeString(visual.name)
    }

}