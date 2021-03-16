package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanInverse
import world.gregs.voidps.network.readShortAdd

class FloorItemOption5Decoder : Decoder(7) {

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