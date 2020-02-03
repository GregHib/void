package org.redrune.network.codec.handshake.decoder

import org.redrune.network.codec.handshake.message.ServiceType
import org.redrune.network.codec.handshake.message.impl.ServiceHandshakeMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class LoginServerHandshakeMessageDecoder : MessageDecoder(intArrayOf(14), 0) {
    override fun decode(reader: PacketReader): Message {
        return ServiceHandshakeMessage(ServiceType.LOGIN)
    }
}