package org.redrune.network.rs.codec.update.decode

import org.redrune.core.network.codec.message.MessageDecoder
import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageDecoder<M : Message> : MessageDecoder<M>()