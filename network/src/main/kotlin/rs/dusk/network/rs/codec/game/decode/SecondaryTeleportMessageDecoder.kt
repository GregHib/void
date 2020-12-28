package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.SecondaryTeleportMessage

class SecondaryTeleportMessageDecoder : MessageDecoder<SecondaryTeleportMessage>(4) {

    override fun decode(packet: PacketReader) = SecondaryTeleportMessage(
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE)
    )

}