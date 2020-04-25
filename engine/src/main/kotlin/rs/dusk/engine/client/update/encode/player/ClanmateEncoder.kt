package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.player.Clanmate

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class ClanmateEncoder : VisualEncoder<Clanmate>(Clanmate::class) {

    override fun encode(writer: Writer, visual: Clanmate) {
        writer.writeByte(visual.clanmate, Modifier.INVERSE)
    }

}