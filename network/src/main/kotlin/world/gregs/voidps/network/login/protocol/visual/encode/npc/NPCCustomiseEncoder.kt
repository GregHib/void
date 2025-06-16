package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_CUSTOMISE

class NPCCustomiseEncoder : VisualEncoder<NPCVisuals>(NPC_CUSTOMISE) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
       //TODO
    }
}