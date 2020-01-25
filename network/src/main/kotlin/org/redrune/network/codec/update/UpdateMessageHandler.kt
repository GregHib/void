package org.redrune.network.codec.update

import org.redrune.network.message.Message
import org.redrune.network.message.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 9:13 p.m.
 */
abstract class UpdateMessageHandler<T : Message> : MessageHandler<T>() {}
