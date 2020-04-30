package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.IN_OUT_SCREEN
import rs.dusk.network.rs.codec.game.decode.message.WindowHoveredMessage

@PacketMetaData(opcodes = [IN_OUT_SCREEN], length = 4)
class WindowHoveredMessageDecoder : GameMessageDecoder<WindowHoveredMessage>() {

    override fun decode(packet: PacketReader) = WindowHoveredMessage(packet.readBoolean())

}