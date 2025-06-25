package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_COLOUR_OVERLAY_MASK

class PlayerColourOverlayEncoder : VisualEncoder<PlayerVisuals>(PLAYER_COLOUR_OVERLAY_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (delay, duration, colour) = visuals.colourOverlay
        writer.apply {
            val saturation = colour and 0xFF
            val hue = colour shr 8 and 0xFF
            val multiplier = colour shr 16 and 0xFF
            val luminance = colour shr 24 and 0xFF

            p1Alt3(hue)
            p1Alt3(saturation)
            p1Alt2(luminance)
            writeByte(multiplier)
            p2Alt3(delay)
            writeShort(duration)
        }
    }

}