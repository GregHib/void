package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class WalkMapDecoder : Decoder(5) {

    override fun decode(player: Player, packet: Reader) {
        handler?.walk(
            player = player,
            y = packet.readShortLittle(),
            running = packet.readBooleanAdd(),
            x = packet.readShortAdd()
        )
    }

}