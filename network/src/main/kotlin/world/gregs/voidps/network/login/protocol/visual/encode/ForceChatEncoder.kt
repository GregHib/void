package world.gregs.voidps.network.login.protocol.visual.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.Visuals

class ForceChatEncoder(mask: Int) : world.gregs.voidps.network.login.protocol.visual.VisualEncoder<Visuals>(mask) {

    override fun encode(writer: Writer, visuals: Visuals) {
        writer.writeString(visuals.forceChat.text)
    }

}