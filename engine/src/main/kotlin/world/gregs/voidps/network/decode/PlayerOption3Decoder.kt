package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readByteSubtract

class PlayerOption3Decoder(handler: Handler? = null) : Decoder(3, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        packet.readByteSubtract()
        handler?.playerOption(
            player = player,
            index = packet.readShortLittleEndian().toInt(),
            optionIndex = 3
        )
    }

}