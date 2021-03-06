package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class PingDecoder : Decoder(0) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.ping(session)
    }

}