package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class WorldListRefreshDecoder(handler: Handler? = null) : Decoder(4, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.refreshWorldList(
            player = player,
            full = packet.readInt() == 0
        )
    }

}