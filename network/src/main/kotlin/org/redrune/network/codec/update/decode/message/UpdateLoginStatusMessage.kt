package org.redrune.network.codec.update.decode.message

import org.redrune.core.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateLoginStatusMessage(val online: Boolean, val value: Int) : Message