package org.redrune.network.codec.update.decode

import org.redrune.core.network.model.message.Message
import org.redrune.core.network.model.message.codec.MessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageDecoder<M : Message> : MessageDecoder<M>()