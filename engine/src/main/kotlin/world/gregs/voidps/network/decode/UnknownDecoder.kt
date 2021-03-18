package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class UnknownDecoder(handler: Handler? = null) : Decoder(2, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.unknown(
            player = player,
            value = packet.readShort().toInt()
        )
    }

}