package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override fun decode(player: Player, packet: Reader) {
        handler?.interfaceSwitch(
            player = player,
            fromHash = packet.readInt(),
            toSlot = packet.readShortLittle(),
            toHash = packet.readUnsignedIntMiddle(),
            fromType = packet.readShort(),
            fromSlot = packet.readShortAddLittle(),
            toType = packet.readShortAddLittle()
        )
    }

}