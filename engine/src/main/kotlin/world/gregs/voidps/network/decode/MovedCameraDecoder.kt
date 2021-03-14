package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class MovedCameraDecoder : Decoder(4) {

    override fun decode(player: Player, packet: Reader) {
        handler?.cameraMoved(
            player = player,
            packet.readUnsignedShort(),
            packet.readUnsignedShort()
        )
    }

}