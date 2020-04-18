package rs.dusk.network.rs.codec.login.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.login.LoginMessageEncoder
import rs.dusk.network.rs.codec.login.encode.message.LobbyLoginConnectionResponseMessage


/**
 * This class encodes the login response code to the client after a game connection request is received.
 * The opcode is always 0.
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LobbyLoginConnectionResponseMessageEncoder : LoginMessageEncoder<LobbyLoginConnectionResponseMessage>() {

    override fun encode(builder: PacketWriter, msg: LobbyLoginConnectionResponseMessage) {
        builder.writeByte(msg.opcode)
    }
}