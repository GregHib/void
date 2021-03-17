package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*

class FloorItemOption3Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.floorItemOption(
            player = player,
            id = packet.readShort().toInt(),
            x = packet.readShortAdd(),
            run = packet.readBoolean(),
            y = packet.readUnsignedShortAddLittle(),
            optionIndex = 2
        )
    }

}