package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class WalkMapDecoder : Decoder(5) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.walk(
            session = session,
            y = packet.readShortLittle(),
            running = packet.readBooleanAdd(),
            x = packet.readShortAdd()
        )
    }

}