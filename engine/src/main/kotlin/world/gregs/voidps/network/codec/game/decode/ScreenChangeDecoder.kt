package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class ScreenChangeDecoder : Decoder(6) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.changeScreen(
            session = session,
            displayMode = packet.readUnsignedByte(),
            width = packet.readUnsignedShort(),
            height = packet.readUnsignedShort(),
            antialiasLevel = packet.readUnsignedByte()
        )
    }

}