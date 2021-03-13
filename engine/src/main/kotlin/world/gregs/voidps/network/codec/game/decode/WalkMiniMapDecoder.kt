package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder

class WalkMiniMapDecoder : Decoder(18) {

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.minimapWalk(
            session = session,
            y = packet.readShortLittle(),
            running = packet.readBooleanAdd(),
            x = packet.readShortAdd()
        )
        packet.readByte()//-1
        packet.readByte()//-1
        packet.readShort()//Rotation?
        packet.readByte()//57
        val minimapRotation = packet.readByte()
        val minimapZoom = packet.readByte()
        packet.readByte()//89
        packet.readShort()//X in region?
        packet.readShort()//Y in region?
        packet.readByte()//63
    }

}