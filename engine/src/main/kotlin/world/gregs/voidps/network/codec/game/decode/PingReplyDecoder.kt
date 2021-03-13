package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class PingReplyDecoder : Decoder(8) {

    override fun decode(player: Player, packet: Reader) {
        handler?.pingReply(
            player = player,
            packet.readInt(),
            packet.readInt()
        )
    }

}