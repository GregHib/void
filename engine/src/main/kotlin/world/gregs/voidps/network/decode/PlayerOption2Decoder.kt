package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class PlayerOption2Decoder : Decoder(3) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        packet.readByte()
        handler?.playerOption(
            player = player,
            index = packet.readShort().toInt(),
            optionIndex = 2
        )
    }

}