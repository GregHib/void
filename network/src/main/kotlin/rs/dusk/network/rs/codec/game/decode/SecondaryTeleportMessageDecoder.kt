package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.OTHER_TELEPORT
import rs.dusk.network.rs.codec.game.decode.message.SecondaryTeleportMessage

@PacketMetaData(opcodes = [OTHER_TELEPORT], length = 4)
class SecondaryTeleportMessageDecoder : GameMessageDecoder<SecondaryTeleportMessage>() {

    override fun decode(packet: PacketReader) = SecondaryTeleportMessage(
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE)
    )

}