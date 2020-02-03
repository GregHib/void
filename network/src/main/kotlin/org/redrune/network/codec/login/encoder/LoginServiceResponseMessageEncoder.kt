package org.redrune.network.codec.login.encoder

import org.redrune.network.codec.login.message.LoginServiceResponseMessage
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class LoginServiceResponseMessageEncoder : MessageEncoder<LoginServiceResponseMessage>() {
    override fun encode(out: PacketBuilder, msg: LoginServiceResponseMessage) {
        out.writeByte(msg.responseCode)
    }
}