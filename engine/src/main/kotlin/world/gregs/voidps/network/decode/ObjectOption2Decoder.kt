package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readShortAddLittle

class ObjectOption2Decoder : Decoder(7) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            y = packet.readShortAddLittle(),
            x = packet.readShortAdd(),
            run = packet.readBooleanSubtract(),
            objectId = packet.readShortAddLittle(),
            option = 2
        )
    }

}