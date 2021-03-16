package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readUnsignedIntMiddle

class APCoordinateDecoder : Decoder(12) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.apCoordinate(
            player = player,
            first = packet.readShortAdd(),
            second = packet.readShortLittleEndian().toInt(),
            third = packet.readUnsignedIntMiddle(),
            fourth = packet.readShortAdd(),
            fifth = packet.readShort().toInt()
        )
    }
}