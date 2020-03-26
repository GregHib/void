package org.redrune.network.codec.update.decode.message

import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateRequestMessage(val indexId: Int, val archiveId: Int, val priority: Boolean) : Message