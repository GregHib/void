package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readShortAddLittle

class ObjectOption5Decoder : Decoder(7) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            y = packet.readShortLittleEndian().toInt(),
            run = packet.readBooleanAdd(),
            x = packet.readShortAddLittle(),
            objectId = packet.readShortAdd(),
            option = 4
        )
    }

}