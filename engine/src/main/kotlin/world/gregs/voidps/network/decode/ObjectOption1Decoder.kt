package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readUnsignedShortAddLittle

class ObjectOption1Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            run = packet.readBooleanSubtract(),
            x = packet.readUnsignedShortAddLittle(),
            y = packet.readShortLittleEndian().toInt(),
            objectId = packet.readUShort().toInt(),
            option = 1
        )
    }

}