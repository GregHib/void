package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBoolean

class WindowFocusDecoder : Decoder(1) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.windowFocus(
            player = player,
            focused = packet.readBoolean()
        )
    }

}