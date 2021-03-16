package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*

class InterfaceOnFloorItemDecoder(handler: Handler? = null) : Decoder(15, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.interfaceOnFloorItem(
            player = player,
            x = packet.readShort().toInt(),
            y = packet.readShort().toInt(),
            floorType = packet.readShortAddLittle(),
            hash = packet.readIntInverseMiddle(),
            slot = packet.readShortLittleEndian().toInt(),
            run = packet.readBoolean(),
            item = packet.readShortLittleEndian().toInt()
        )
    }

}