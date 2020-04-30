package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.WALK
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage

@PacketMetaData(opcodes = [WALK], length = 5)
class WalkMapMessageDecoder : GameMessageDecoder<WalkMapMessage>() {

    override fun decode(packet: PacketReader) = WalkMapMessage(
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readBoolean()
    )

}