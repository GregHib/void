package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.NPC_TURN_MASK

class NPCTurnEncoder : VisualEncoder<NPCVisuals>(NPC_TURN_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (targetX, targetY) = visuals.turn
        writer.apply {
            writeShortAdd(targetX * 2 + 1)
            writeShortLittle(targetY * 2 + 1)
        }
    }

}