package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

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