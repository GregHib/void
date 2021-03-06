package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class KeysPressedDecoder : Decoder(BYTE) {

    override fun decode(session: ClientSession, packet: Reader) {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.readableBytes() > 0) {
            keys.add(packet.readUnsignedByte() to packet.readUnsignedShort())
        }
        handler?.keysPressed(session, keys)
    }

}