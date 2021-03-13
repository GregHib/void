package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class APCoordinateDecoder : Decoder(12) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.apCoordinate(
            session,
            packet.readShortAdd(),
            packet.readShortLittle(),
            packet.readUnsignedIntMiddle(),
            packet.readShortAdd(),
            packet.readShort()
        )
    }
}