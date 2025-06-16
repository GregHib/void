package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_EXACT_MOVEMENT_MASK

class PlayerExactMovementEncoder : VisualEncoder<PlayerVisuals>(PLAYER_EXACT_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (tile1X, tile1Y, delay1, tile2X, tile2Y, delay2, direction) = visuals.exactMovement
        writer.apply {
            writeByteSubtract(tile1X)
            writeByte(tile1Y)
            writeByteAdd(tile2X)
            writeByteAdd(tile2Y)
            writeShortLittle(delay1)
            writeShortAddLittle(delay2)
            writeByteAdd(direction / 2)
        }
    }

}