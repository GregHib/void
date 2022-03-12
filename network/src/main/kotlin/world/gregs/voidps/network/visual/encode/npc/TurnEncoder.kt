package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.TURN_MASK

class TurnEncoder : VisualEncoder<NPCVisuals>(TURN_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (x, y, directionX, directionY) = visuals.turn
        writer.apply {
            writeShortAdd((x + directionX) * 2 + 1)
            writeShortLittle((y + directionY) * 2 + 1)
        }
    }

}