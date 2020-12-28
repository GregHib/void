package rs.dusk.network.rs.codec.game.decode

import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.decode.message.PublicMessage
import rs.dusk.utility.inject

class PublicMessageDecoder : MessageDecoder<PublicMessage>(VARIABLE_LENGTH_BYTE) {

    private val huffman: Huffman by inject()

    override fun decode(packet: PacketReader): PublicMessage {
        val colour = packet.readUnsignedByte()
        val move = packet.readUnsignedByte()
        val message = huffman.decompress(packet, packet.readSmart())
        val effects = colour shl 8 or (move and 0xff)
        return PublicMessage(message, effects)
    }

}