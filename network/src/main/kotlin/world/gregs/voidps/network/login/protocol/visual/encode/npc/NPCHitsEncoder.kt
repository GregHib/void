package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_HITS_MASK

class NPCHitsEncoder : VisualEncoder<NPCVisuals>(NPC_HITS_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals, index: Int) {
        val (damage, player, other) = visuals.hits
        writer.apply {
            writeByteSubtract(damage.count { it != null })
            for (hit in damage) {
                if (hit == null) {
                    break
                }
                hit.write(writer, index, player, add = false)
            }
        }
    }

    override fun encode(writer: Writer, visuals: NPCVisuals): Unit = throw RuntimeException("Shouldn't be reachable")
}
