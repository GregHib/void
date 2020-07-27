package rs.dusk.network.rs.codec.game.decode

import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PRIVATE_MESSAGE
import rs.dusk.network.rs.codec.game.decode.message.PrivateMessage
import rs.dusk.utility.inject

@PacketMetaData(opcodes = [PRIVATE_MESSAGE], length = PacketType.VARIABLE_LENGTH_SHORT)
class PrivateMessageDecoder : GameMessageDecoder<PrivateMessage>() {

    private val huffman: Huffman by inject()

    override fun decode(packet: PacketReader) =
        PrivateMessage(packet.readString(), huffman.decompress(packet, packet.readSmart()))

}