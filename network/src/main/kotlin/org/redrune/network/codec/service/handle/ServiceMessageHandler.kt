package org.redrune.network.codec.service.handle

import org.redrune.core.network.model.message.Message
import org.redrune.core.network.model.message.codec.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageHandler<M : Message> : MessageHandler<M>()