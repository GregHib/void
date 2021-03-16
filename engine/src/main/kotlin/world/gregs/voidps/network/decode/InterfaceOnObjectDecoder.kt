package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*

class InterfaceOnObjectDecoder(handler: Handler? = null) : Decoder(15, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.interfaceOnObject(
            player = player,
            y = packet.readShortAdd(),
            slot = packet.readShortAddLittle(),
            hash = packet.readIntLittleEndian(),
            type = packet.readShortAdd(),
            run = packet.readBooleanSubtract(),
            x = packet.readShortLittleEndian().toInt(),
            id = packet.readUnsignedShortLittle()
        )
    }

}