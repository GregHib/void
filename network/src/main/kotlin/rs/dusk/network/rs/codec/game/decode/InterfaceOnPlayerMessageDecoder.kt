package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ON_PLAYER
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnPlayerMessage

@PacketMetaData(opcodes = [INTERFACE_ON_PLAYER], length = 1)
class InterfaceOnPlayerMessageDecoder : GameMessageDecoder<InterfaceOnPlayerMessage>() {

    override fun decode(packet: PacketReader) = InterfaceOnPlayerMessage(
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(order = Endian.LITTLE),
        packet.readShort(),
        packet.readBoolean(Modifier.SUBTRACT),
        packet.readShort(Modifier.ADD, Endian.LITTLE)
    )

}