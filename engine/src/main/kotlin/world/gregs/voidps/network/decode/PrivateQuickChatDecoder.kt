package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readString

class PrivateQuickChatDecoder(handler: Handler? = null) : Decoder(BYTE, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.privateQuickChat(
            player = player,
            name = packet.readString(),
            file = packet.readUShort().toInt(),
            data = packet.readBytes(packet.remaining.toInt())
        )
    }

}