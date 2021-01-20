package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.secure.Huffman
import world.gregs.void.network.codec.Decoder
import world.gregs.void.network.packet.PacketSize.SHORT
import world.gregs.void.utility.inject

class PrivateDecoder : Decoder(SHORT) {

    private val huffman: Huffman by inject()

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.privateMessage(
            context,
            name = packet.readString(),
            message = huffman.decompress(packet, packet.readSmart())
        )
    }

}