package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readUnsignedShortAdd

class ObjectOption4Decoder : Decoder(7) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            run = packet.readBooleanAdd(),
            objectId = packet.readUnsignedShortAdd(),
            x = packet.readShortAdd(),
            y = packet.readShortLittleEndian().toInt(),
            option = 4
        )
    }

}