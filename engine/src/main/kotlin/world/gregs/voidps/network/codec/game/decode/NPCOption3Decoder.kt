package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class NPCOption3Decoder : Decoder(3) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.npcOption(
            session = session,
            npcIndex = packet.readShort(),
            run = packet.readBoolean(),
            option = 3
        )
    }

}