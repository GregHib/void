package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class RegionLoadingDecoder : Decoder(4) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        packet.readInt()//1057001181
        handler?.regionLoading(player)
    }

}