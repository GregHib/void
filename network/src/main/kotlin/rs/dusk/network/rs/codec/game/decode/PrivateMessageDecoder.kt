package rs.dusk.network.rs.codec.game.decode

import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_SHORT
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.PrivateMessage
import rs.dusk.utility.inject

class PrivateMessageDecoder : GameMessageDecoder<PrivateMessage>(VARIABLE_LENGTH_SHORT) {

    private val huffman: Huffman by inject()

    override fun decode(packet: PacketReader) =
        PrivateMessage(packet.readString(), huffman.decompress(packet, packet.readSmart()))

}