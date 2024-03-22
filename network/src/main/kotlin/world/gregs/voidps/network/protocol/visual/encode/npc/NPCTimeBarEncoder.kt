package world.gregs.voidps.network.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.protocol.visual.NPCVisuals
import world.gregs.voidps.network.protocol.visual.VisualEncoder
import world.gregs.voidps.network.protocol.visual.VisualMask.NPC_TIME_BAR_MASK

class NPCTimeBarEncoder : VisualEncoder<NPCVisuals>(NPC_TIME_BAR_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (full, exponentialDelay, delay, increment) = visuals.timeBar
        writer.apply {
            writeShort(((if (full) 1 else 0) * 0x8000) or (exponentialDelay and 0x7fff))
            writeByteSubtract(delay)
            writeByteSubtract(increment)
        }
    }

}