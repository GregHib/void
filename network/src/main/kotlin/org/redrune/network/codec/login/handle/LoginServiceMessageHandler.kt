package org.redrune.network.codec.login.handle

import org.redrune.network.message.Message
import org.redrune.network.message.codec.MessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginServiceMessageHandler<M : Message> : MessageHandler<M>()