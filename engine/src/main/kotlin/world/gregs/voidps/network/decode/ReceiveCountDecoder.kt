package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class ReceiveCountDecoder : Decoder(4) {

    override fun decode(player: Player, packet: Reader) {
        handler?.receiveCount(
            player = player,
            count = packet.readInt()
        )
    }

}