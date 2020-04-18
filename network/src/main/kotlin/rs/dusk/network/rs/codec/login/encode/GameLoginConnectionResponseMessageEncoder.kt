package rs.dusk.network.rs.codec.login.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.login.LoginMessageEncoder
import rs.dusk.network.rs.codec.login.encode.message.GameLoginConnectionResponseMessage


/**
 * This class encodes the login response code to the client after a game connection request is received.
 * The opcode is always 0.
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameLoginConnectionResponseMessageEncoder : LoginMessageEncoder<GameLoginConnectionResponseMessage>() {

    override fun encode(builder: PacketWriter, msg: GameLoginConnectionResponseMessage) {
        builder.writeOpcode(msg.opcode, PacketType.FIXED)
    }
}