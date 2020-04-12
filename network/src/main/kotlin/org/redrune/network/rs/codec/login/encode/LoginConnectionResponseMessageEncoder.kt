package org.redrune.network.rs.codec.login.encode

import org.redrune.core.network.codec.packet.access.PacketWriter
import org.redrune.network.rs.codec.login.LoginMessageEncoder
import org.redrune.network.rs.codec.login.encode.message.LoginConnectionResponseMessage


/**
 * This class encodes the login response code to the client after a game connection request is received.
 * The opcode is always 0.
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LoginConnectionResponseMessageEncoder : LoginMessageEncoder<LoginConnectionResponseMessage>() {

    override fun encode(builder: PacketWriter, msg: LoginConnectionResponseMessage) {
        builder.writeByte(msg.opcode)
    }
}