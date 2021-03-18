package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanInverse
import world.gregs.voidps.network.readShortAdd

class FloorItemOption2Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.floorItemOption(
            player = player,
            y = packet.readShortAdd(),
            id = packet.readShortAdd(),
            x = packet.readShortLittleEndian().toInt(),
            run = packet.readBooleanInverse(),
            optionIndex = 1
        )
    }

}