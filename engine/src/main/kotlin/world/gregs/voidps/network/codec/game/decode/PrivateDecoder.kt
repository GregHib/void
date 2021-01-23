package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.SHORT
import world.gregs.voidps.utility.inject

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