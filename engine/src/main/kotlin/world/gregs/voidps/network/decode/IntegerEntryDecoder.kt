package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class IntegerEntryDecoder : Decoder(4) {

    override fun decode(player: Player, packet: Reader) {
        handler?.integerEntered(
            player = player,
            packet.readInt()
        )
    }

}