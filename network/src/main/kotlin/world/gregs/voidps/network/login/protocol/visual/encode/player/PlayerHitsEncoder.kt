package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_HITS_MASK

class PlayerHitsEncoder : VisualEncoder<PlayerVisuals>(PLAYER_HITS_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals, index: Int) {
        val (damage, player, other) = visuals.hits
        writer.apply {
            writeByte(damage.size)
            damage.forEach { hit ->
                hit.writePlayer(writer, index, player, add = true)
            }
        }
    }

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        throw RuntimeException("Shouldn't be reachable")
    }

}