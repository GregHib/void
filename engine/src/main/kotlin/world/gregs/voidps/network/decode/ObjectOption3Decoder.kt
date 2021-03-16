package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readUnsignedShortAddLittle

class ObjectOption3Decoder : Decoder(7) {

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