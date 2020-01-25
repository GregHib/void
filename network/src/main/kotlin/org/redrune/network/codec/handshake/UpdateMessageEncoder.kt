package org.redrune.network.codec.handshake

import org.redrune.network.message.Message
import org.redrune.network.message.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 9:17 p.m.
 */
abstract class UpdateMessageEncoder<T: Message> : MessageEncoder<T>()