package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readByteInverse
import world.gregs.voidps.network.readString

class FriendChatRankDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.rankFriendsChat(
            player = player,
            name = packet.readString(),
            rank = packet.readByteInverse()
        )
    }

}