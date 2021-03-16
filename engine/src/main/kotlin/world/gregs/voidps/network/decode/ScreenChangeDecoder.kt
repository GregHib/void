package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readUnsignedByte

class ScreenChangeDecoder(handler: Handler? = null) : Decoder(6, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.changeScreen(
            player = player,
            displayMode = packet.readUnsignedByte(),
            width = packet.readUShort().toInt(),
            height = packet.readUShort().toInt(),
            antialiasLevel = packet.readUnsignedByte()
        )
    }

}