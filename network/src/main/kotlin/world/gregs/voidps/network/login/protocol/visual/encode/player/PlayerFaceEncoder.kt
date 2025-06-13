package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_FACE_MASK

class PlayerFaceEncoder : VisualEncoder<PlayerVisuals>(PLAYER_FACE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.writeShort(visuals.face.direction)
    }
}
