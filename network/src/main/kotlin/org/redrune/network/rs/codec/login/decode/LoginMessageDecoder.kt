package org.redrune.network.rs.codec.login.decode

import org.redrune.core.network.codec.message.MessageDecoder
import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageDecoder<M : Message> : MessageDecoder<M>()