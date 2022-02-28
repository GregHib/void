package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.PlayerVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_FORCE_MOVEMENT_MASK

class PlayerForceMovementEncoder : VisualEncoder<PlayerVisuals>(PLAYER_FORCE_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (tile1, delay1, tile2, delay2, direction) = visuals.forceMovement
        writer.apply {
            writeByte(tile1.x)
            writeByteSubtract(tile1.y)
            writeByteInverse(tile2.x)
            writeByteInverse(tile2.y)
            writeShortAddLittle(delay1)
            writeShortAdd(delay2)
            writeByte(direction.ordinal / 2)
        }
    }

}