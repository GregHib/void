package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class ChatTypeDecoder(handler: Handler? = null) : Decoder(1, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.changeChatType(
            player = player,
            type = packet.readUByte().toInt()
        )
    }

}