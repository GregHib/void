package org.redrune.network.codec.service.decoder

import org.redrune.network.codec.service.message.ServiceType
import org.redrune.network.codec.service.message.impl.SimpleServiceMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class LoginServiceMessageDecoder : MessageDecoder(0, 14) {
    override fun decode(reader: PacketReader): Message {
        return SimpleServiceMessage(ServiceType.LOGIN)
    }
}