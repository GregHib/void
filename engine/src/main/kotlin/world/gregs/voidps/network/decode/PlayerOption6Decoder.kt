package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readShortAdd

class PlayerOption6Decoder : Decoder(3) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        packet.readByte()
        handler?.playerOption(
            player = player,
            index = packet.readShortAdd(),
            optionIndex = 6
        )
    }

}