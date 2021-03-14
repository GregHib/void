package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

    override fun decode(player: Player, packet: Reader) {
        handler?.interfaceOption(
            player = player,
            packet.readInt(),
            packet.readShort(),
            packet.readShort(),
            index
        )
    }

}