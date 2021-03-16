package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readShortAdd

class FloorItemOption1Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.floorItemOption(
            player = player,
            run = packet.readBooleanSubtract(),
            x = packet.readShortLittleEndian().toInt(),
            y = packet.readShortAdd(),
            id = packet.readShort().toInt(),
            optionIndex = 0
        )
    }

}