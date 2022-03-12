package world.gregs.voidps.network.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.MOVEMENT_TYPE_MASK

class MovementTypeEncoder : VisualEncoder<PlayerVisuals>(MOVEMENT_TYPE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.writeByteInverse(visuals.movementType.type.id)
    }

}