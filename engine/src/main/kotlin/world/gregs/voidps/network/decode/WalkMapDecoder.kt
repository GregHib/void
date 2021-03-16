package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readUnsignedShortAdd

class WalkMapDecoder : Decoder(5) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.walk(
            player = player,
            y = packet.readShortLittleEndian().toInt(),
            running = packet.readBooleanAdd(),
            x = packet.readUnsignedShortAdd()
        )
    }

}