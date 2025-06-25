package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_GRAPHIC_2_MASK

class PlayerSecondaryGraphicEncoder : VisualEncoder<PlayerVisuals>(PLAYER_GRAPHIC_2_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val visual = visuals.graphics[1]
        writer.apply {
            p2Alt3(visual.id)
            writeIntInverseMiddle(visual.packedDelayHeight)
            p1Alt2(visual.packedRotationRefresh)
        }
    }

}