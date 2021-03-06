package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE
import world.gregs.voidps.utility.inject

class PublicDecoder : Decoder(BYTE) {

    private val huffman: Huffman by inject()

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.publicMessage(
            session = session,
            effects = packet.readUnsignedByte() shl 8 or (packet.readUnsignedByte() and 0xff),
            message = huffman.decompress(packet, packet.readSmart())
        )
    }

}