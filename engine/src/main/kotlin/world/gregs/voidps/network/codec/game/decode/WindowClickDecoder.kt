package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class WindowClickDecoder : Decoder(6) {

    override fun decode(player: Player, packet: Reader) {
        handler?.windowClick(
            player = player,
            hash = packet.readShort(),
            position = packet.readInt()
        )
    }

}