package org.redrune.network.codec.service.decode.message

import org.redrune.core.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateHandshakeMessage(val major: Int) : Message