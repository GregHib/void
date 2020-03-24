package org.redrune.network.codec.login.decode

import org.redrune.core.network.message.Message
import org.redrune.core.network.message.codec.MessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageDecoder<M : Message> : MessageDecoder<M>()