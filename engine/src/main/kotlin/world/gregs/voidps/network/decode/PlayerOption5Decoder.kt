package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class PlayerOption5Decoder : Decoder(3) {

    override fun decode(player: Player, packet: Reader) {
        handler?.playerOption(
            player = player,
            index = packet.readShortAdd(),
            optionIndex = 5
        )
        packet.readByte()
    }

}