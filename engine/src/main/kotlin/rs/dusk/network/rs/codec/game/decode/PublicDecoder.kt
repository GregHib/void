package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader
import rs.dusk.core.network.codec.packet.PacketSize.BYTE
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