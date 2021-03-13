package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class WindowFocusDecoder : Decoder(1) {

    override fun decode(player: Player, packet: Reader) {
        handler?.windowFocus(
            player = player,
            focused = packet.readBoolean()
        )
    }

}