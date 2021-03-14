package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class APCoordinateDecoder : Decoder(12) {

    override fun decode(player: Player, packet: Reader) {
        handler?.apCoordinate(
            player = player,
            packet.readShortAdd(),
            packet.readShortLittle(),
            packet.readUnsignedIntMiddle(),
            packet.readShortAdd(),
            packet.readShort()
        )
    }
}