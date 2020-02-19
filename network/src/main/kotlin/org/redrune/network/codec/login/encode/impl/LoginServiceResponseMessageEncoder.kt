package org.redrune.network.codec.login.encode.impl

import org.redrune.network.codec.login.encode.LoginServiceMessageEncoder
import org.redrune.network.codec.login.encode.message.LoginServiceResponseMessage
import org.redrune.network.packet.access.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LoginServiceResponseMessageEncoder : LoginServiceMessageEncoder<LoginServiceResponseMessage>() {

    override fun encode(builder: PacketBuilder, msg: LoginServiceResponseMessage) {
        builder.writeByte(msg.opcode)
    }
}