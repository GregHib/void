package org.redrune.network.codec.login.encoder

import org.redrune.network.codec.login.message.LoginResponseMessage
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
class LoginResponseMessageEncoder : MessageEncoder<LoginResponseMessage>() {
    override fun encode(out: PacketBuilder, msg: LoginResponseMessage) {
        out.writeByte(msg.responseCode)
    }
}