package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class PingReplyDecoder : Decoder(8) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.pingReply(
            player = player,
            first = packet.readInt(),
            second = packet.readInt()
        )
    }

}