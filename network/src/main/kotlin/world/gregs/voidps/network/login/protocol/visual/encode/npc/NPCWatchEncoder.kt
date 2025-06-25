package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_WATCH_MASK

class NPCWatchEncoder : VisualEncoder<NPCVisuals>(NPC_WATCH_MASK) {
    override fun encode(writer: Writer, visuals: NPCVisuals) {
        writer.ip2(visuals.watch.index)
    }
}