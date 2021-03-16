package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBoolean

class WindowHoveredDecoder(handler: Handler? = null) : Decoder(4, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.windowHovered(
            player = player,
            over = packet.readBoolean()
        )
    }

}