package org.redrune.network.codec.update.encode

import org.redrune.core.network.message.Message
import org.redrune.core.network.message.codec.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageEncoder<M: Message> : MessageEncoder<M>()