package org.redrune.network.codec.handshake.message.impl

import org.redrune.network.codec.handshake.message.ServiceType
import org.redrune.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @property type ServiceType
 * @since 2020-02-02
 */
class ServiceHandshakeMessage(val type: ServiceType) : Message