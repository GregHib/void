package org.redrune.network.codec.update.message

import org.redrune.network.message.Message

/**
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 8:49 p.m.
 */
data class ClientConnectionMessage(val connectionId: Int) : Message