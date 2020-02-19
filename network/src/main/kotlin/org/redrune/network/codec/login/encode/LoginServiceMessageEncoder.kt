package org.redrune.network.codec.login.encode

import org.redrune.network.message.Message
import org.redrune.network.message.codec.MessageEncoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginServiceMessageEncoder<M : Message> : MessageEncoder<M>()