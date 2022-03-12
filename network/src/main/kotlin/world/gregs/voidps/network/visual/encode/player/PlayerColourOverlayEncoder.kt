package world.gregs.voidps.network.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.PLAYER_COLOUR_OVERLAY_MASK

class PlayerColourOverlayEncoder : VisualEncoder<PlayerVisuals>(PLAYER_COLOUR_OVERLAY_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (delay, duration, colour) = visuals.colourOverlay
        writer.apply {
            val hue = colour and 0xFF
            val saturation = colour shr 8 and 0xFF
            val luminance = colour shr 16 and 0xFF
            val multiplier = colour shr 24 and 0xFF
            writeByteInverse(hue)
            writeByte(saturation)
            writeByte(luminance)
            writeByte(multiplier)
            writeShortAddLittle(delay)
            writeShortAdd(duration)
        }
    }

}