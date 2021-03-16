package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class MovedMouseDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.mouseMoved(player)
    }
}