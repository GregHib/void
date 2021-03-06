package world.gregs.voidps.network.codec.game.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.SHORT
import world.gregs.voidps.utility.inject

class PrivateDecoder : Decoder(SHORT) {

    private val huffman: Huffman by inject()

    override fun decode(session: ClientSession, packet: Reader) {
        handler?.privateMessage(
            session = session,
            name = packet.readString(),
            message = huffman.decompress(packet, packet.readSmart())
        )
    }

}