package org.redrune.network.codec.service.encode

import org.redrune.core.network.message.Message
import org.redrune.core.network.message.codec.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageEncoder<M: Message> : MessageEncoder<M>()