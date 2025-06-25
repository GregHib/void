package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_GRAPHIC_1_MASK

class PlayerPrimaryGraphicEncoder : VisualEncoder<PlayerVisuals>(PLAYER_GRAPHIC_1_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val visual = visuals.graphics[0]
        writer.apply {
            writeShort(visual.id)
            writeIntLittle(visual.packedDelayHeight)
            writeByteAdd(visual.packedRotationRefresh)
        }
    }

}