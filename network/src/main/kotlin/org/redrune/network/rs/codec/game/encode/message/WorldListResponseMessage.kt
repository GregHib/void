package org.redrune.network.rs.codec.game.encode.message

import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
data class WorldListResponseMessage(val full: Boolean) : Message