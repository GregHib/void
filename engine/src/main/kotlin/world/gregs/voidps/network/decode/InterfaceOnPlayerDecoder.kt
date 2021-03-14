package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class InterfaceOnPlayerDecoder : Decoder(1) {

    override fun decode(player: Player, packet: Reader) {
        handler?.interfaceOnPlayer(
            player = player,
            playerIndex = packet.readShortAddLittle(),
            type = packet.readShortLittle(),
            slot = packet.readShortLittle(),
            hash = packet.readIntInverseMiddle(),
            run = packet.readBooleanInverse()
        )
    }

}