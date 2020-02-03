package org.redrune.network.codec.handshake.decoder

import com.github.michaelbull.logging.InlineLogger
import org.redrune.network.codec.handshake.message.ServiceType
import org.redrune.network.codec.handshake.message.impl.ServiceVersionHandshakeMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class UpdateServerHandshakeMessageDecoder : MessageDecoder(intArrayOf(15), 4) {

    private val logger = InlineLogger()

    override fun decode(reader: PacketReader): Message {
        val version = reader.readUnsignedInt()
        logger.info { "read version = $version" }
        return ServiceVersionHandshakeMessage(ServiceType.UPDATE, version)
    }
}