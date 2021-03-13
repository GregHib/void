package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class ObjectOption2Decoder : Decoder(7) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.objectOption(
            session = session,
            y = packet.readShortAddLittle(),
            x = packet.readShortAdd(),
            run = packet.readBooleanSubtract(),
            objectId = packet.readShortAddLittle(),
            option = 2
        )
    }

}