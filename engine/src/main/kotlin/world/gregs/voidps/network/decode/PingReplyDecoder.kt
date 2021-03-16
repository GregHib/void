package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class PingReplyDecoder(handler: Handler? = null) : Decoder(8, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.pingReply(
            player = player,
            first = packet.readInt(),
            second = packet.readInt()
        )
    }

}