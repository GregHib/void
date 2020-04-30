package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CLICK
import rs.dusk.network.rs.codec.game.decode.message.WindowClickMessage

@PacketMetaData(opcodes = [CLICK], length = 6)
class WindowClickMessageDecoder : GameMessageDecoder<WindowClickMessage>() {

    override fun decode(packet: PacketReader) =
        WindowClickMessage(
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readInt(order = Endian.MIDDLE)
        )

}