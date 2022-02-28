package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.PlayerVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_HITS_MASK

class PlayerHitsEncoder : VisualEncoder<PlayerVisuals>(PLAYER_HITS_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (damage, player, other) = visuals.hits
        writer.apply {
            writeByteInverse(damage.size)
            damage.forEach { hit ->
                hit.write(writer, player, other, add = true)
            }
        }
    }

}