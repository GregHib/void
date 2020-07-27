package rs.dusk.network.rs.codec.game.decode

import rs.dusk.cache.secure.Huffman
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PUBLIC_MESSAGE
import rs.dusk.network.rs.codec.game.decode.message.PublicMessage
import rs.dusk.utility.inject

@PacketMetaData(opcodes = [PUBLIC_MESSAGE], length = PacketType.VARIABLE_LENGTH_BYTE)
class PublicMessageDecoder : GameMessageDecoder<PublicMessage>() {

    private val huffman: Huffman by inject()

    override fun decode(packet: PacketReader): PublicMessage {
        val colour = packet.readUnsignedByte()
        val move = packet.readUnsignedByte()
        val message = huffman.decompress(packet, packet.readSmart())
        val effects = colour shl 8 or (move and 0xff)
        return PublicMessage(message, effects)
    }

}