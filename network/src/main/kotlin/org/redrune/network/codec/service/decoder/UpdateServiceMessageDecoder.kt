package org.redrune.network.codec.service.decoder

import com.github.michaelbull.logging.InlineLogger
import org.redrune.network.codec.service.message.ServiceType
import org.redrune.network.codec.service.message.impl.VersionBoundServiceMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class UpdateServiceMessageDecoder : MessageDecoder(4, 15) {

    private val logger = InlineLogger()

    override fun decode(reader: PacketReader): Message {
        val version = reader.readUnsignedInt()
        logger.info { "read version = $version" }
        return VersionBoundServiceMessage(ServiceType.UPDATE, version)
    }
}