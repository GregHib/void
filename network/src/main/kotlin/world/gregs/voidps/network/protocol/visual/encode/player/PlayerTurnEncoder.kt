package world.gregs.voidps.network.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.protocol.visual.VisualEncoder
import world.gregs.voidps.network.protocol.visual.VisualMask.PLAYER_TURN_MASK

class PlayerTurnEncoder : VisualEncoder<PlayerVisuals>(PLAYER_TURN_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.writeShort(visuals.turn.direction)
    }

}