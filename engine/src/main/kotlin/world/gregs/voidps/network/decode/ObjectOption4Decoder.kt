package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class ObjectOption4Decoder : Decoder(7) {

    override fun decode(player: Player, packet: Reader) {
        handler?.objectOption(
            player = player,
            run = packet.readBooleanAdd(),
            objectId = packet.readShortAdd(),
            x = packet.readShortAdd(),
            y = packet.readShortLittle(),
            option = 4
        )
    }

}