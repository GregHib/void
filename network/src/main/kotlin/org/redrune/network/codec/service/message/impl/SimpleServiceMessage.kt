package org.redrune.network.codec.service.message.impl

import org.redrune.network.codec.service.message.ServiceType
import org.redrune.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @property type ServiceType
 * @since 2020-02-02
 */
data class SimpleServiceMessage(val type: ServiceType) : Message