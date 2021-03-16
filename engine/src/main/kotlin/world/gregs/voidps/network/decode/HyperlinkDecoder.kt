package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readString

class HyperlinkDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.hyperlink(
            player = player,
            name = packet.readString(),
            script = packet.readString(),
            third = packet.readByte().toInt()
        )
    }

}