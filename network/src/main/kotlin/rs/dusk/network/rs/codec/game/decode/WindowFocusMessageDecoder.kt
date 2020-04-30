package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.TOGGLE_FOCUS
import rs.dusk.network.rs.codec.game.decode.message.WindowFocusMessage

@PacketMetaData(opcodes = [TOGGLE_FOCUS], length = 1)
class WindowFocusMessageDecoder : GameMessageDecoder<WindowFocusMessage>() {

    override fun decode(packet: PacketReader) = WindowFocusMessage(packet.readBoolean())

}