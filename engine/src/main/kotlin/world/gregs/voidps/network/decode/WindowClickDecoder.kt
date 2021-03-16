package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class WindowClickDecoder(handler: Handler? = null) : Decoder(6, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.windowClick(
            player = player,
            hash = packet.readShort().toInt(),
            position = packet.readInt()
        )
    }

}