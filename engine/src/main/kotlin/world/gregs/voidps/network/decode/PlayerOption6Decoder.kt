package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class PlayerOption6Decoder : Decoder(3) {

    override fun decode(player: Player, packet: Reader) {
        packet.readByte()
        handler?.playerOption(
            player = player,
            index = packet.readShortAdd(),
            optionIndex = 6
        )
    }

}