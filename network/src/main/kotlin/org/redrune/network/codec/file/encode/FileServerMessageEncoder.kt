package org.redrune.network.codec.file.encode

import org.redrune.network.message.codec.MessageEncoder
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class FileServerMessageEncoder<M: Message> : MessageEncoder<M>()