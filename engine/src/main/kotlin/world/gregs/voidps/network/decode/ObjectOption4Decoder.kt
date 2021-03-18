package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readUnsignedShortAdd

class ObjectOption4Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            run = packet.readBooleanAdd(),
            objectId = packet.readUnsignedShortAdd(),
            x = packet.readUnsignedShortAdd(),
            y = packet.readShortLittleEndian().toInt(),
            option = 4
        )
    }

}