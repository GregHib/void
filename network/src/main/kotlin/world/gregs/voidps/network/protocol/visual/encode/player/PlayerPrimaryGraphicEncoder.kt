package world.gregs.voidps.network.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.protocol.visual.VisualEncoder
import world.gregs.voidps.network.protocol.visual.VisualMask.PLAYER_GRAPHIC_1_MASK

class PlayerPrimaryGraphicEncoder : VisualEncoder<PlayerVisuals>(PLAYER_GRAPHIC_1_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val visual = visuals.primaryGraphic
        writer.apply {
            writeShortAddLittle(visual.id)
            writeIntInverseMiddle(visual.packedDelayHeight)
            writeByteAdd(visual.packedRotationRefresh)
        }
    }

}