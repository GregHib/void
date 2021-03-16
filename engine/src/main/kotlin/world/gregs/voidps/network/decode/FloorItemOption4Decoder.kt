package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readShortAdd

class FloorItemOption4Decoder : Decoder(7) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.floorItemOption(
            player = player,
            run = packet.readBooleanSubtract(),
            x = packet.readShortAdd(),
            y = packet.readShortLittleEndian().toInt(),
            id = packet.readShort().toInt(),
            optionIndex = 3
        )
    }

}