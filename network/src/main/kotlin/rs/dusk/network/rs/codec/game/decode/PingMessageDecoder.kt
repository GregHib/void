package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PING
import rs.dusk.network.rs.codec.game.decode.message.PingMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
@PacketMetaData(opcodes = [PING], length = 0)
class PingMessageDecoder : GameMessageDecoder<PingMessage>() {

    override fun decode(packet: PacketReader): PingMessage {
        println("Ping")
        return PingMessage
    }

}