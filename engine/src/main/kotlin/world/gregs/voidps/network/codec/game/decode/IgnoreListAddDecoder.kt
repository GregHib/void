package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class IgnoreListAddDecoder : Decoder(BYTE) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.addIgnore(
            session,
            packet.readString(),
            packet.readBoolean()
        )
    }

}