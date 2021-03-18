package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanInverse
import world.gregs.voidps.network.readShortAdd

class FloorItemOption5Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.floorItemOption(
            player = player,
            y = packet.readShort().toInt(),
            x = packet.readShortAdd(),
            run = packet.readBooleanInverse(),
            id = packet.readShortAdd(),
            optionIndex = 4
        )
    }

}