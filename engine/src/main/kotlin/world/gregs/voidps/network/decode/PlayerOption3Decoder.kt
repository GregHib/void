package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class PlayerOption3Decoder : Decoder(3) {

    override fun decode(player: Player, packet: Reader) {
        packet.readByteSubtract()
        handler?.playerOption(
            player = player,
            index = packet.readShortLittle(),
            optionIndex = 3
        )
    }

}