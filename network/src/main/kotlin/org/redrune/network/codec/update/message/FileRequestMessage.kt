package org.redrune.network.codec.update.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 2:51 a.m.
 */
data class FileRequestMessage(val indexId: Int, val archiveId: Int, val priority: Boolean) : Message

