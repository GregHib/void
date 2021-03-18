package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class ScreenChangeDecoder(handler: Handler? = null) : Decoder(6, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.changeScreen(
            player = player,
            displayMode = packet.readUByte().toInt(),
            width = packet.readUShort().toInt(),
            height = packet.readUShort().toInt(),
            antialiasLevel = packet.readUByte().toInt()
        )
    }

}