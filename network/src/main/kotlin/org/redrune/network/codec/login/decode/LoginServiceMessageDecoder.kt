package org.redrune.network.codec.login.decode

import org.redrune.network.message.Message
import org.redrune.network.message.codec.MessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginServiceMessageDecoder<M : Message> : MessageDecoder<M>()