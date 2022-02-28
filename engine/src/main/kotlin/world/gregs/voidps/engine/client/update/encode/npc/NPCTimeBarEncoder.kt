package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.NPCVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.NPC_TIME_BAR_MASK
import world.gregs.voidps.engine.entity.character.update.visual.TimeBar
import world.gregs.voidps.engine.utility.toInt

class NPCTimeBarEncoder : VisualEncoder<NPCVisuals>(NPC_TIME_BAR_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val visual = visuals.aspects[mask] as TimeBar
        val (full, exponentialDelay, delay, increment) = visual
        writer.apply {
            writeShort((full.toInt() * 0x8000) or (exponentialDelay and 0x7fff))
            writeByteSubtract(delay)
            writeByteSubtract(increment)
        }
    }

}