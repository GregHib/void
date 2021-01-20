package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.secure.Huffman
import world.gregs.void.network.codec.Decoder
import world.gregs.void.network.packet.PacketSize.BYTE
import world.gregs.void.utility.inject

class PublicDecoder : Decoder(BYTE) {

    private val huffman: Huffman by inject()

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.publicMessage(
            context = context,
            effects = packet.readUnsignedByte() shl 8 or (packet.readUnsignedByte() and 0xff),
            message = huffman.decompress(packet, packet.readSmart())
        )
    }

}