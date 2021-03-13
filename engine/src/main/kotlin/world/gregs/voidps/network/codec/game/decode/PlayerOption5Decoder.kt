package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class PlayerOption5Decoder : Decoder(3) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.playerOption(
            session = session,
            index = packet.readShortAdd(),
            optionIndex = 5
        )
        packet.readByte()
    }

}