package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_WATCH_MASK

class PlayerWatchEncoder : VisualEncoder<PlayerVisuals>(PLAYER_WATCH_MASK) {
    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.p2Alt2(visuals.watch.index)
    }
}