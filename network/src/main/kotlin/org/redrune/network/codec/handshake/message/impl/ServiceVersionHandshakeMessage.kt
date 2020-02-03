package org.redrune.network.codec.handshake.message.impl

import org.redrune.network.codec.handshake.message.ServiceType
import org.redrune.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @property type The type of service being requested
 * @property majorBuild Int The major build version of the client
 * @since 2020-02-02
 */
data class ServiceVersionHandshakeMessage(
    val type: ServiceType,
    val majorBuild: Int
) : Message
