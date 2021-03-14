package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class FloorItemOption4Decoder : Decoder(7) {

    override fun decode(player: Player, packet: Reader) {
        handler?.floorItemOption(
            player = player,
            run = packet.readBooleanSubtract(),
            x = packet.readShortAdd(),
            y = packet.readShortLittle(),
            id = packet.readShort(),
            optionIndex = 3
        )
    }

}