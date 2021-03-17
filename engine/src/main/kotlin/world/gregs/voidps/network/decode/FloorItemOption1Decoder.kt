package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanSubtract

class FloorItemOption1Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.floorItemOption(
            player = player,
            id = packet.readShort().toInt(),
            x = packet.readShort().toInt(),
            y = packet.readShort().toInt(),
            run = packet.readBooleanSubtract(),
            optionIndex = 0
        )
    }

}