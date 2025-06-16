package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_COLOUR_OVERLAY_MASK

class NPCColourOverlayEncoder : VisualEncoder<NPCVisuals>(NPC_COLOUR_OVERLAY_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (delay, duration, colour) = visuals.colourOverlay
        writer.apply {
            val hue = colour and 0xFF
            val saturation = colour shr 8 and 0xFF
            val luminance = colour shr 16 and 0xFF
            val multiplier = colour shr 24 and 0xFF
            writeByteAdd(hue)
            writeByteSubtract(saturation)
            writeByte(luminance)
            writeByteSubtract(multiplier)
            writeShortLittle(delay)
            writeShort(duration)
        }
    }

}