package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class SecondaryTeleportDecoder : Decoder(4) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.secondaryTeleport(
            session = session,
            x = packet.readShortAddLittle(),
            y = packet.readShortLittle()
        )
    }

}