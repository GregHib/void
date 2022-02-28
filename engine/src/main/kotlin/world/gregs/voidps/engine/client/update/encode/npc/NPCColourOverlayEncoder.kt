package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.ColourOverlay
import world.gregs.voidps.engine.entity.character.update.visual.NPC_COLOUR_OVERLAY_MASK

class NPCColourOverlayEncoder : VisualEncoder(NPC_COLOUR_OVERLAY_MASK) {

    override fun encode(writer: Writer, visuals: Visuals) {
        val visual = visuals.aspects[mask] as ColourOverlay
        val (delay, duration, colour) = visual
        writer.apply {
            val hue = colour and 0xFF
            val saturation = colour shr 8 and 0xFF
            val luminance = colour shr 16 and 0xFF
            val multiplier = colour shr 24 and 0xFF
            writeByteInverse(hue)
            writeByteSubtract(saturation)
            writeByteInverse(luminance)
            writeByte(multiplier)
            writeShort(delay)
            writeShort(duration)
        }
    }

}