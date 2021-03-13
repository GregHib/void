package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.interfaceOnInterface(
            session = session,
            fromHash = packet.readInt(),
            toHash = packet.readIntInverseMiddle(),
            fromItem = packet.readShortAdd(),
            from = packet.readShort(),
            toItem = packet.readShortAdd(),
            to = packet.readShort()
        )
    }

}