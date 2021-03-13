package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption3Decoder : Decoder(7) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.floorItemOption(
            session = session,
            id = packet.readShort(),
            x = packet.readShortAdd(),
            run = packet.readBoolean(),
            y = packet.readShortAddLittle(),
            optionIndex = 2
        )
    }

}