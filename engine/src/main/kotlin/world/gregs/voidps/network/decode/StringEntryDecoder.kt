package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readString

class StringEntryDecoder(handler: Handler? = null) : Decoder(BYTE, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.stringEntered(
            player = player,
            text = packet.readString()
        )
    }

}