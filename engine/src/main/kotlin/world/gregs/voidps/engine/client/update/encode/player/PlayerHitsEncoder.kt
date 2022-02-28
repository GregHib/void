package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.Hits
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_HITS_MASK

class PlayerHitsEncoder : VisualEncoder(PLAYER_HITS_MASK) {

    override fun encode(writer: Writer, visuals: Visuals) {
        val visual = visuals.aspects[mask] as Hits
        val (damage, player, other) = visual
        writer.apply {
            writeByteInverse(damage.size)
            damage.forEach { hit ->
                hit.write(writer, player, other, add = true)
            }
        }
    }

}