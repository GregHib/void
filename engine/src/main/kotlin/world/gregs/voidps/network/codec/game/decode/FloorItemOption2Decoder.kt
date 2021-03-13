package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption2Decoder : Decoder(7) {

    override fun decode(player: Player, packet: Reader) {
        handler?.floorItemOption(
            player = player,
            y = packet.readShortAdd(),
            id = packet.readShortAdd(),
            x = packet.readShortLittle(),
            run = packet.readBooleanInverse(),
            optionIndex = 1
        )
    }

}