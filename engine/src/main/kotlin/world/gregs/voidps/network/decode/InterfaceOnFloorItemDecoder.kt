package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class InterfaceOnFloorItemDecoder : Decoder(15) {

    override fun decode(player: Player, packet: Reader) {
        handler?.interfaceOnFloorItem(
            player = player,
            packet.readShort(),
            packet.readShort(),
            packet.readShortAddLittle(),
            packet.readIntInverseMiddle(),
            packet.readShortLittle(),
            packet.readBoolean(),
            packet.readShortLittle()
        )
    }

}