package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class UnknownScriptDecoder : Decoder(BYTE) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.unknownScript(
            session = session,
            string = packet.readString()
        )
    }

}