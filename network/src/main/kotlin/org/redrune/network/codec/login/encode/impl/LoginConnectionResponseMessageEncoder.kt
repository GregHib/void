package org.redrune.network.codec.login.encode.impl

import org.redrune.core.network.packet.access.PacketBuilder
import org.redrune.network.codec.login.encode.LoginMessageEncoder
import org.redrune.network.codec.login.encode.message.LoginConnectionResponseMessage


/**
 * This class encodes the login response code to the client after a game connection request is received.
 * The opcode is always 0.
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LoginConnectionResponseMessageEncoder : LoginMessageEncoder<LoginConnectionResponseMessage>() {

    override fun encode(builder: PacketBuilder, msg: LoginConnectionResponseMessage) {
        builder.writeByte(msg.opcode)
    }
}