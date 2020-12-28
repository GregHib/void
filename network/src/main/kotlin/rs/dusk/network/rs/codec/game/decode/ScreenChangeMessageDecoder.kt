package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.ScreenChangeMessage

class ScreenChangeMessageDecoder : MessageDecoder<ScreenChangeMessage>(6) {

    override fun decode(packet: PacketReader) = ScreenChangeMessage(
        packet.readUnsignedByte(),
        packet.readUnsignedShort(),
        packet.readUnsignedShort(),
        packet.readUnsignedByte()
    )

}