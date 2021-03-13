package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class NPCOption1Decoder : Decoder(3) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.npcOption(
            session = session,
            run = packet.readBoolean(),
            npcIndex = packet.readShortLittle(),
            option = 1
        )
    }

}