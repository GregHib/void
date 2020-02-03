package org.redrune.network.codec.login.encoder

import org.redrune.network.codec.login.message.LoginHandshakeResponseMessage
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class LoginHandshakeResponseMessageEncoder : MessageEncoder<LoginHandshakeResponseMessage>() {
    override fun encode(out: PacketBuilder, msg: LoginHandshakeResponseMessage) {
        out.writeByte(msg.responseCode)
    }
}