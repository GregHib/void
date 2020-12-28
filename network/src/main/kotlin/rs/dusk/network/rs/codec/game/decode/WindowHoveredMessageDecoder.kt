package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.WindowHoveredMessage

class WindowHoveredMessageDecoder : MessageDecoder<WindowHoveredMessage>(4) {

    override fun decode(packet: PacketReader) = WindowHoveredMessage(packet.readBoolean())

}