package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Decoder

class ObjectOption1Decoder : Decoder(7) {

    override fun decode(player: Player, packet: Reader) {
        handler?.objectOption(
            player = player,
            run = packet.readBooleanSubtract(),
            x = packet.readShortAddLittle(),
            y = packet.readShortLittle(),
            objectId = packet.readShort(),
            option = 1
        )
    }

}