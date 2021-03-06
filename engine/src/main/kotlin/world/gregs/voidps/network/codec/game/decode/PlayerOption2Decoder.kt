package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class PlayerOption2Decoder : Decoder(3) {

    override fun decode(session: ClientSession, packet: Reader) {
        packet.readByte()
        handler?.playerOption(
            session = session,
            index = packet.readShort(),
            optionIndex = 2
        )
    }

}