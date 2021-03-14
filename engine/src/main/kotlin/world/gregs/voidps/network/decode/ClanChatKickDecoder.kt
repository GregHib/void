package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.PacketSize.BYTE

class ClanChatKickDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        handler?.kickClanMember(
            player = player,
            packet.readBoolean(),
            packet.readShort(),
            packet.readString()
        )
    }

}