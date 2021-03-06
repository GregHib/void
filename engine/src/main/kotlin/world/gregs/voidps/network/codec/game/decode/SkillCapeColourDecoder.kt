package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class SkillCapeColourDecoder : Decoder(2) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.skillCapeColour(
            session = session,
            colour = packet.readUnsignedShort()
        )
    }

}