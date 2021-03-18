package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.*

class InterfaceOnNpcDecoder(handler: Handler? = null) : Decoder(11, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.interfaceOnNPC(
            player = player,
            slot = packet.readShortAddLittle(),
            hash = packet.readInt(),
            type = packet.readShortLittleEndian().toInt(),
            run = packet.readBooleanAdd(),
            npc = packet.readShortAdd()
        )
    }

}