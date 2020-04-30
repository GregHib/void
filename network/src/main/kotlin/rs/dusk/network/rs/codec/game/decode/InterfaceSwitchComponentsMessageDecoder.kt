package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.SWITCH_INTERFACE_COMPONENTS
import rs.dusk.network.rs.codec.game.decode.message.InterfaceSwitchComponentsMessage

@PacketMetaData(opcodes = [SWITCH_INTERFACE_COMPONENTS], length = 16)
class InterfaceSwitchComponentsMessageDecoder : GameMessageDecoder<InterfaceSwitchComponentsMessage>() {

    override fun decode(packet: PacketReader) = InterfaceSwitchComponentsMessage(
        packet.readShort(),
        packet.readShort(order = Endian.LITTLE),
        packet.readShort(Modifier.ADD),
        packet.readInt(order = Endian.MIDDLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE)
    )

}