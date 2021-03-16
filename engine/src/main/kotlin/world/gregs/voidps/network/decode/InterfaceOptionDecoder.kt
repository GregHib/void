package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

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