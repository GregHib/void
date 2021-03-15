package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class FriendChatRankDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        handler?.rankFriendsChat(
            player = player,
            packet.readString(),
            packet.readByteInverse()
        )
    }

}