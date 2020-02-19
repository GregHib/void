package org.redrune.network.codec.file.decode.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class FileServerRequestMessage(val indexId: Int, val archiveId: Int, val priority: Boolean) :
    Message