package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanInverse
import world.gregs.voidps.network.readIntInverseMiddle
import world.gregs.voidps.network.readShortAddLittle

class InterfaceOnPlayerDecoder : Decoder(1) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.interfaceOnPlayer(
            player = player,
            playerIndex = packet.readShortAddLittle(),
            type = packet.readShortLittleEndian().toInt(),
            slot = packet.readShortLittleEndian().toInt(),
            hash = packet.readIntInverseMiddle(),
            run = packet.readBooleanInverse()
        )
    }

}