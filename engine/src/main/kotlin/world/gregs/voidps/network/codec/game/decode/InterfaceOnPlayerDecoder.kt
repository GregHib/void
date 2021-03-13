package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnPlayerDecoder : Decoder(1) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.interfaceOnPlayer(
            session = session,
            player = packet.readShortAddLittle(),
            type = packet.readShortLittle(),
            slot = packet.readShortLittle(),
            hash = packet.readIntInverseMiddle(),
            run = packet.readBooleanInverse()
        )
    }

}