package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.player.CLANMATE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.Clanmate

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class ClanmateEncoder : VisualEncoder<Clanmate>(CLANMATE_MASK) {

    override fun encode(writer: Writer, visual: Clanmate) {
        writer.writeByte(visual.clanmate, Modifier.ADD)
    }

}