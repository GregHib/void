package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnObjectDecoder : Decoder(15) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.interfaceOnObject(
            session = session,
            y = packet.readShortAdd(),
            slot = packet.readShortAddLittle(),
            hash = packet.readIntLittle(),
            type = packet.readShortAdd(),
            run = packet.readBooleanSubtract(),
            x = packet.readShortLittle(),
            id = packet.readUnsignedShortLittle()
        )
    }

}