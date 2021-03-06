package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class WindowClickDecoder : Decoder(6) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.windowClick(
            session = session,
            hash = packet.readShort(),
            position = packet.readInt()
        )
    }

}