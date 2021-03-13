package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.interfaceSwitch(
            session = session,
            fromHash = packet.readInt(),
            toSlot = packet.readShortLittle(),
            toHash = packet.readUnsignedIntMiddle(),
            fromType = packet.readShort(),
            fromSlot = packet.readShortAddLittle(),
            toType = packet.readShortAddLittle()
        )
    }

}