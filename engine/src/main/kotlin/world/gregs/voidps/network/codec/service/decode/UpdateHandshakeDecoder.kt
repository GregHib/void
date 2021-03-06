package world.gregs.voidps.network.codec.service.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class UpdateHandshakeDecoder : Decoder(4) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.updateHandshake(
            context = context,
            version = packet.readInt()
        )
    }

}
