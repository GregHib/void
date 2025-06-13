package world.gregs.voidps.network.login.protocol.visual.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.Visuals

class SayEncoder(mask: Int) : VisualEncoder<Visuals>(mask) {

    override fun encode(writer: Writer, visuals: Visuals) {
        writer.writeString(visuals.say.text)
    }
}
