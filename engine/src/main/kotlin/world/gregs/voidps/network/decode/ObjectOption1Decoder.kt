package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.io.bits.reverseByteOrder
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readShortAddLittle

class ObjectOption1Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            run = packet.readBooleanSubtract(),
            x = packet.readShortAddLittle(),
            y = packet.readUShort().reverseByteOrder().toInt(),
            objectId = packet.readUShort().toInt(),
            option = 1
        )
    }

}