package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_CUSTOMISE_HEAD

class NPCCustomiseChatHeadEncoder : VisualEncoder<NPCVisuals>(NPC_CUSTOMISE_HEAD) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        // TODO
    }
}