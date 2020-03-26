package org.redrune.network.codec.update.encode

import org.redrune.core.network.model.message.Message
import org.redrune.core.network.model.message.codec.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageEncoder<M: Message> : MessageEncoder<M>()