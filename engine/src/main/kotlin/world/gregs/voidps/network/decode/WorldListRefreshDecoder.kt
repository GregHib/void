package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class WorldListRefreshDecoder : Decoder(4) {

    override fun decode(player: Player, packet: Reader) {
        handler?.refreshWorldList(
            player = player,
            full = packet.readInt() == 0
        )
    }

}