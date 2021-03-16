package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class PlayerOption2Decoder(handler: Handler? = null) : Decoder(3, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        packet.readByte()
        handler?.playerOption(
            player = player,
            index = packet.readShort().toInt(),
            optionIndex = 2
        )
    }

}