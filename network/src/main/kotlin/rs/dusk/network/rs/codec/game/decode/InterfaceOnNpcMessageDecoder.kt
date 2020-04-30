package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ON_NPC
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnNpcMessage

@PacketMetaData(opcodes = [INTERFACE_ON_NPC], length = 11)
class InterfaceOnNpcMessageDecoder : GameMessageDecoder<InterfaceOnNpcMessage>() {

    override fun decode(packet: PacketReader) = InterfaceOnNpcMessage(
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readBoolean()
    )

}