package org.redrune.network.codec.handshake.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 3:05 p.m.
 */
class LoginHandshakeResponse(val statusCode: Int) : Message {
}