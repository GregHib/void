package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class ReceiveCountDecoder : Decoder(4) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.receiveCount(
            player = player,
            count = packet.readInt()
        )
    }

}