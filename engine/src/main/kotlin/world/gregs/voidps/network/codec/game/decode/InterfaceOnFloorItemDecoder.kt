package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnFloorItemDecoder : Decoder(15) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.interfaceOnFloorItem(
            session,
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