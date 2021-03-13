package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption5Decoder : Decoder(7) {

    override fun decode(player: Player, packet: Reader) {
        handler?.floorItemOption(
            player = player,
            y = packet.readShort(),
            x = packet.readShortAdd(),
            run = packet.readBooleanInverse(),
            id = packet.readShortAdd(),
            optionIndex = 4
        )
    }

}