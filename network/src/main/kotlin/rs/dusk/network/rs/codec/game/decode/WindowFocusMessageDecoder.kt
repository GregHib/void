package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.WindowFocusMessage

class WindowFocusMessageDecoder : GameMessageDecoder<WindowFocusMessage>(1) {

    override fun decode(packet: PacketReader) = WindowFocusMessage(packet.readBoolean())

}