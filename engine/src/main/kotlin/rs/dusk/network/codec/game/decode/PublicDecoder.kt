package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.secure.Huffman
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader
import rs.dusk.network.packet.PacketSize.BYTE
import rs.dusk.utility.inject

class PublicDecoder : Decoder(BYTE) {

    private val huffman: Huffman by inject()

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.publicMessage(
            context = context,
            effects = packet.readUnsignedByte() shl 8 or (packet.readUnsignedByte() and 0xff),
            message = huffman.decompress(packet, packet.readSmart())
        )
    }

}