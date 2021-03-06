package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class WindowFocusDecoder : Decoder(1) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.windowFocus(
            session = session,
            focused = packet.readBoolean()
        )
    }

}