package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.SCREEN_CHANGE
import rs.dusk.network.rs.codec.game.decode.message.ScreenChangeMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
@PacketMetaData(opcodes = [SCREEN_CHANGE], length = 6)
class ScreenChangeMessageDecoder : GameMessageDecoder<ScreenChangeMessage>() {

    override fun decode(packet: PacketReader) =
        ScreenChangeMessage(
            packet.readUnsignedByte(),
            packet.readUnsignedShort(),
            packet.readUnsignedShort(),
            packet.readUnsignedByte()
        )

}