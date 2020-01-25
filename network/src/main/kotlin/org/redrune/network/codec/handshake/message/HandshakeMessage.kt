package org.redrune.network.codec.handshake.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 12:48 a.m.
 */
data class HandshakeMessage(val version: Int) : Message