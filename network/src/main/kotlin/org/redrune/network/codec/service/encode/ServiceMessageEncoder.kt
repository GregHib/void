package org.redrune.network.codec.service.encode

import org.redrune.core.network.model.message.Message
import org.redrune.core.network.model.message.codec.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageEncoder<M: Message> : MessageEncoder<M>()