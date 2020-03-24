package org.redrune.network.codec.service.handle

import org.redrune.core.network.message.Message
import org.redrune.core.network.message.codec.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageHandler<M : Message> : MessageHandler<M>()