package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class LobbyOnlineStatusDecoder : Decoder(3) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.lobbyOnlineStatus(
            session,
            packet.readByte(),
            packet.readByte(),
            packet.readByte()
        )
    }

}