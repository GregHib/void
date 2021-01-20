package world.gregs.void.engine.client.update.encode.player

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.player.CLANMATE_MASK
import world.gregs.void.engine.entity.character.update.visual.player.Clanmate

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class ClanmateEncoder : VisualEncoder<Clanmate>(CLANMATE_MASK) {

    override fun encode(writer: Writer, visual: Clanmate) {
        writer.writeByte(visual.clanmate, Modifier.INVERSE)
    }

}