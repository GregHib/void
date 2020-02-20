package org.redrune.network.codec.login.encode.impl

import org.redrune.network.codec.login.encode.LoginMessageEncoder
import org.redrune.network.codec.login.encode.message.LoginServiceResponseMessage
import org.redrune.network.packet.access.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LoginResponseMessageEncoder : LoginMessageEncoder<LoginServiceResponseMessage>() {

    override fun encode(builder: PacketBuilder, msg: LoginServiceResponseMessage) {
        builder.writeByte(msg.opcode)
    }
}