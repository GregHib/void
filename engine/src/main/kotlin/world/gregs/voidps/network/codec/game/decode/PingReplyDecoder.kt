package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class PingReplyDecoder : Decoder(8) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.pingReply(
            session,
            packet.readInt(),
            packet.readInt()
        )
    }

}