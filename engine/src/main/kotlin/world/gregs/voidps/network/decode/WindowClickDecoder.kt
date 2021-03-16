package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class WindowClickDecoder : Decoder(6) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.windowClick(
            player = player,
            hash = packet.readShort().toInt(),
            position = packet.readInt()
        )
    }

}