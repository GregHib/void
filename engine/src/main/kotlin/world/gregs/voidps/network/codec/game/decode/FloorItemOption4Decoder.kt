package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption4Decoder : Decoder(7) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.floorItemOption(
            session = session,
            run = packet.readBooleanSubtract(),
            x = packet.readShortAdd(),
            y = packet.readShortLittle(),
            id = packet.readShort(),
            optionIndex = 3
        )
    }

}