package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.character.update.VisualEncoder
import rs.dusk.engine.entity.character.update.visual.player.CLANMATE_MASK
import rs.dusk.engine.entity.character.update.visual.player.Clanmate

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class ClanmateEncoder : VisualEncoder<Clanmate>(CLANMATE_MASK) {

    override fun encode(writer: Writer, visual: Clanmate) {
        writer.writeByte(visual.clanmate, Modifier.INVERSE)
    }

}