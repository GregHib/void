package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.SCREEN_CLOSE
import rs.dusk.network.rs.codec.game.decode.message.ScreenCloseMessage

@PacketMetaData(opcodes = [SCREEN_CLOSE], length = 0)
class ScreenCloseMessageDecoder : GameMessageDecoder<ScreenCloseMessage>() {

    override fun decode(packet: PacketReader) = ScreenCloseMessage

}