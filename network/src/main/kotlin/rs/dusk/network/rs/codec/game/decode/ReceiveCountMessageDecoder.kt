package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.RECEIVE_COUNT
import rs.dusk.network.rs.codec.game.decode.message.ReceiveCountMessage

@PacketMetaData(opcodes = [RECEIVE_COUNT], length = 4)
class ReceiveCountMessageDecoder : GameMessageDecoder<ReceiveCountMessage>() {

    override fun decode(packet: PacketReader) = ReceiveCountMessage(packet.readInt())

}