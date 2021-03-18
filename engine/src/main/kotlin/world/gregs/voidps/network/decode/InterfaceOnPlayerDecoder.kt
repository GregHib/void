package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*

class InterfaceOnPlayerDecoder(handler: Handler? = null) : Decoder(1, handler) {

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