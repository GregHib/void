package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.utility.inject

class PublicMessageDecoder : MessageDecoder(VARIABLE_LENGTH_BYTE) {

    private val huffman: Huffman by inject()

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.publicMessage(
            context = context,
            effects = packet.readUnsignedByte() shl 8 or (packet.readUnsignedByte() and 0xff),
            message = huffman.decompress(packet, packet.readSmart())
        )
    }

}