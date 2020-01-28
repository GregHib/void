package org.redrune.network.codec.handshake.response

import org.redrune.network.codec.handshake.request.HandshakeRequestType
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 27, 2020
 */
class HandshakeResponseMessage(val requestType: HandshakeRequestType, val opcode: Int) : Message