package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage

class WalkMapMessageDecoder : MessageDecoder<WalkMapMessage>(5) {

    override fun decode(packet: PacketReader) = WalkMapMessage(
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readBoolean()
    )

}