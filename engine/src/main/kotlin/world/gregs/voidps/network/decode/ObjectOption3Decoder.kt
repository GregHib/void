package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*

class ObjectOption3Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            y = packet.readShortAdd(),
            objectId = packet.readUnsignedShortAddLittle(),
            x = packet.readShortLittleEndian().toInt(),
            run = packet.readBooleanAdd(),
            option = 3
        )
    }

}