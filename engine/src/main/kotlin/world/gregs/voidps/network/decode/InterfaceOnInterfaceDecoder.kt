package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readIntInverseMiddle
import world.gregs.voidps.network.readShortAdd

class InterfaceOnInterfaceDecoder(handler: Handler? = null) : Decoder(16, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.interfaceOnInterface(
            player = player,
            fromHash = packet.readInt(),
            toHash = packet.readIntInverseMiddle(),
            fromItem = packet.readShortAdd(),
            from = packet.readShort().toInt(),
            toItem = packet.readShortAdd(),
            to = packet.readShort().toInt()
        )
    }

}