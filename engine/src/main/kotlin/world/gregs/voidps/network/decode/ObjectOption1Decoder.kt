package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readUnsignedShortAddLittle

class ObjectOption1Decoder : Decoder(7) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            run = packet.readBooleanSubtract(),
            x = packet.readUnsignedShortAddLittle(),
            y = packet.readShortLittleEndian().toInt(),
            objectId = packet.readShort().toInt(),
            option = 1
        )
    }

}