package org.redrune.network.codec.file.decode

import org.redrune.network.message.codec.MessageDecoder
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class FileServerMessageDecoder<M : Message> : MessageDecoder<M>()