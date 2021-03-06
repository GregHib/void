package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.interfaceOption(
            session,
            packet.readInt(),
            packet.readShort(),
            packet.readShort(),
            index
        )
    }

}