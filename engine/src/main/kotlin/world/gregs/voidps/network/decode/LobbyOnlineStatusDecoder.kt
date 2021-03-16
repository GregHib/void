package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class LobbyOnlineStatusDecoder : Decoder(3) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.lobbyOnlineStatus(
            player = player,
            first = packet.readByte().toInt(),
            status = packet.readByte().toInt(),
            second = packet.readByte().toInt()
        )
    }

}