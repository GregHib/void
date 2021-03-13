package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class HyperlinkDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        handler?.hyperlink(
            player = player,
            packet.readString(),
            packet.readString(),
            packet.readByte()
        )
    }

}