package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readByteAdd

class PlayerOption4Decoder : Decoder(3) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.playerOption(
            player = player,
            index = packet.readShort().toInt(),
            optionIndex = 4
        )
        packet.readByteAdd()
    }

}