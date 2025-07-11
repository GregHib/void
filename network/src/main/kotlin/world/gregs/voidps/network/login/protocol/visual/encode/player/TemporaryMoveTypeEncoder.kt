package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.TEMPORARY_MOVEMENT_TYPE_MASK

class TemporaryMoveTypeEncoder : VisualEncoder<PlayerVisuals>(TEMPORARY_MOVEMENT_TYPE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.writeByteInverse(visuals.temporaryMoveType.type.id)
    }
}
