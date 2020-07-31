package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.LOGOUT
import rs.dusk.network.rs.codec.game.GameOpcodes.LOGOUT_LOBBY
import rs.dusk.network.rs.codec.game.encode.message.LogoutMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class LogoutMessageEncoder : GameMessageEncoder<LogoutMessage>() {

    override fun encode(builder: PacketWriter, msg: LogoutMessage) {
        val (lobby) = msg
        builder.apply {
            writeOpcode(if (lobby) LOGOUT_LOBBY else LOGOUT)
        }
    }
}