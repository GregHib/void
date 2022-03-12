package world.gregs.voidps.network.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.TEMPORARY_MOVE_TYPE_MASK

class TemporaryMoveTypeEncoder : VisualEncoder<PlayerVisuals>(TEMPORARY_MOVE_TYPE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.writeByteSubtract(visuals.temporaryMoveType.type.id)
    }

}