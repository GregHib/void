package rs.dusk.engine.client.update.encode.npc

import rs.dusk.core.io.write.Writer
import rs.dusk.engine.model.entity.index.update.VisualEncoder
import rs.dusk.engine.model.entity.index.update.visual.npc.NAME_MASK
import rs.dusk.engine.model.entity.index.update.visual.npc.Name

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class NameEncoder : VisualEncoder<Name>(NAME_MASK) {

    override fun encode(writer: Writer, visual: Name) {
        writer.writeString(visual.name)
    }

}