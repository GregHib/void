package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readByteInverse

class PlayerOption1Decoder : Decoder(3) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.playerOption(
            player = player,
            index = packet.readShortLittleEndian().toInt(),
            optionIndex = 1
        )
        packet.readByteInverse()
    }

}