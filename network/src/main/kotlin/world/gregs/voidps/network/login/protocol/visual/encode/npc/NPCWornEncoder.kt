package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_WORN

class NPCWornEncoder : VisualEncoder<NPCVisuals>(NPC_WORN) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        // TODO
    }
}