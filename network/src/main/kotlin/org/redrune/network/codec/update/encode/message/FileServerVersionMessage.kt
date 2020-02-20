package org.redrune.network.codec.update.encode.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class FileServerVersionMessage(val opcode: Int) : Message