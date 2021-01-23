package world.gregs.voidps.engine.client.update.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.ForceChat

/**
 * @author GregHib <greg@gregs.world>
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