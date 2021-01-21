package world.gregs.void.engine.client.update.encode.npc

import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.npc.NAME_MASK
import world.gregs.void.engine.entity.character.update.visual.npc.Name

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class NameEncoder : VisualEncoder<Name>(NAME_MASK) {

    override fun encode(writer: Writer, visual: Name) {
        writer.writeString(visual.name)
    }

}