package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_SHORT
import rs.dusk.utility.inject

class PrivateMessageDecoder : MessageDecoder(VARIABLE_LENGTH_SHORT) {

    private val huffman: Huffman by inject()

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.privateMessage(
            context,
            name = packet.readString(),
            message = huffman.decompress(packet, packet.readSmart())
        )
    }

}