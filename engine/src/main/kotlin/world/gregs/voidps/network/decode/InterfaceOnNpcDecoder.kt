package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readShortAddLittle

class InterfaceOnNpcDecoder : Decoder(11) {

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