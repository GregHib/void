package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class PingDecoder(handler: Handler? = null) : Decoder(0, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.ping(player)
    }

}