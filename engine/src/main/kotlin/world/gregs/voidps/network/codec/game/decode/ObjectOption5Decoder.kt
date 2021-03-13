package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class ObjectOption5Decoder : Decoder(7) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.objectOption(
            session = session,
            y = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            x = packet.readShortAddLittle(),
            objectId = packet.readShortAdd(),
            option = 4
        )
    }

}