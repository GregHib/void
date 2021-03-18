package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*

class ObjectOption2Decoder(handler: Handler? = null) : Decoder(7, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.objectOption(
            player = player,
            y = packet.readShortAddLittle(),
            x = packet.readUnsignedShortAdd(),
            run = packet.readBooleanSubtract(),
            objectId = packet.readUnsignedShortAddLittle(),
            option = 2
        )
    }

}