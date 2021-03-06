package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class RegionLoadingDecoder : Decoder(4) {

    override fun decode(session: ClientSession, packet: Reader) {
        packet.readInt()//1057001181
        handler?.regionLoading(session)
    }

}