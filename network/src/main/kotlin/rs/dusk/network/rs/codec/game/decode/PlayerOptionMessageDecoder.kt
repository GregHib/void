package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.PlayerOptionMessage

class PlayerOptionMessageDecoder(private val index: Int) : MessageDecoder<PlayerOptionMessage>(3) {

    override fun decode(packet: PacketReader): PlayerOptionMessage {
        packet.readByte()//0
        return PlayerOptionMessage(packet.readShort(), index + 1)
    }

}