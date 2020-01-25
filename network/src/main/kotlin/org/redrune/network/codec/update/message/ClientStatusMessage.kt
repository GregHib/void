package org.redrune.network.codec.update.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 9:01 p.m.
 */
data class ClientStatusMessage(val online: Boolean, val value: Int) : Message