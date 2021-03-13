package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class NPCOption5Decoder : Decoder(3) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.npcOption(
            session = session,
            npcIndex = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            option = 5
        )
    }

}