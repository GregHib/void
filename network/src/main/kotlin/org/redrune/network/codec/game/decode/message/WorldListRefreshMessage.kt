package org.redrune.network.codec.game.decode.message

import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 19, 2020
 */
data class WorldListRefreshMessage(val crc: Int) : Message