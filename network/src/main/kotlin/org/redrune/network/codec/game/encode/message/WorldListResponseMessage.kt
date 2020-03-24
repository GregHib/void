package org.redrune.network.codec.game.encode.message

import org.redrune.core.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
data class WorldListResponseMessage(val full: Boolean) : Message