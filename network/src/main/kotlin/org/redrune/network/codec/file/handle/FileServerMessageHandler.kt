package org.redrune.network.codec.file.handle

import org.redrune.network.message.codec.MessageHandler
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class FileServerMessageHandler<M: Message> : MessageHandler<M>()