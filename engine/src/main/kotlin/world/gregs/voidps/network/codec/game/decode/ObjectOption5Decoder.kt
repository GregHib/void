package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class ObjectOption5Decoder : Decoder(7) {

    override fun decode(player: Player, packet: Reader) {
        handler?.objectOption(
            player = player,
            y = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            x = packet.readShortAddLittle(),
            objectId = packet.readShortAdd(),
            option = 4
        )
    }

}