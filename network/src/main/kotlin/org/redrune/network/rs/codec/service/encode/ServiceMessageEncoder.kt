package org.redrune.network.rs.codec.service.encode

import org.redrune.core.network.codec.message.MessageEncoder
import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageEncoder<M: Message> : MessageEncoder<M>()