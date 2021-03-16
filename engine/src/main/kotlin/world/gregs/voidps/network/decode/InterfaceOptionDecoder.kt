package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class InterfaceOptionDecoder(private val index: Int, handler: Handler? = null) : Decoder(8, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.interfaceOption(
            player = player,
            hash = packet.readInt(),
            itemId = packet.readShort().toInt(),
            itemSlot = packet.readShort().toInt(),
            option = index
        )
    }

}