package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBoolean
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readShortAddLittle

class FloorItemOption3Decoder : Decoder(7) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.floorItemOption(
            player = player,
            id = packet.readShort().toInt(),
            x = packet.readShortAdd(),
            run = packet.readBoolean(),
            y = packet.readShortAddLittle(),
            optionIndex = 2
        )
    }

}