package world.gregs.voidps.network.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.PLAYER_FORCE_MOVEMENT_MASK

class PlayerForceMovementEncoder : VisualEncoder<PlayerVisuals>(PLAYER_FORCE_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (tile1X, tile1Y, delay1, tile2X, tile2Y, delay2, direction) = visuals.forceMovement
        writer.apply {
            writeByte(tile1X)
            writeByteSubtract(tile1Y)
            writeByteInverse(tile2X)
            writeByteInverse(tile2Y)
            writeShortAddLittle(delay1)
            writeShortAdd(delay2)
            writeByte(direction / 2)
        }
    }

}