package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class ReportAbuseDecoder : Decoder(BYTE) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.reportAbuse(
            session = session,
            name = packet.readString(),
            type = packet.readByte(),
            integer = packet.readByte(),
            string = packet.readString()
        )
    }

}