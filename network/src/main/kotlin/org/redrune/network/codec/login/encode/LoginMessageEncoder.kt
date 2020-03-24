package org.redrune.network.codec.login.encode

import org.redrune.core.network.message.Message
import org.redrune.core.network.message.codec.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageEncoder<M : Message> : MessageEncoder<M>()