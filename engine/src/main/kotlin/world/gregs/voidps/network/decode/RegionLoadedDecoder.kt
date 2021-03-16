package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

class RegionLoadedDecoder(handler: Handler? = null) : Decoder(0, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.regionLoaded(player)
    }

}