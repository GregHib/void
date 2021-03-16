package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBoolean

class WindowFocusDecoder(handler: Handler? = null) : Decoder(1, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.windowFocus(
            player = player,
            focused = packet.readBoolean()
        )
    }

}