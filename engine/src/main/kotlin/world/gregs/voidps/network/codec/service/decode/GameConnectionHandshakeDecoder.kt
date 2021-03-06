package world.gregs.voidps.network.codec.service.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class GameConnectionHandshakeDecoder : Decoder(0) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.gameHandshake(session)
    }

}