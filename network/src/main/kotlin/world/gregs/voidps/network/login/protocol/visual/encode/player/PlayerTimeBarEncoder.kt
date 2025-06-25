package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_TIME_BAR_MASK

class PlayerTimeBarEncoder : VisualEncoder<PlayerVisuals>(PLAYER_TIME_BAR_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (full, exponentialDelay, delay, increment) = visuals.timeBar
        writer.apply {
            p2Alt2(((if (full) 1 else 0) * 0x8000) or (exponentialDelay and 0x7fff))
            writeByte(delay)
            p1Alt2(increment)
        }
    }

}