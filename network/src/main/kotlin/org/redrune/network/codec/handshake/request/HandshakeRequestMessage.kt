package org.redrune.network.codec.handshake.request

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 27, 2020
 */
data class HandshakeRequestMessage(val type: HandshakeRequestType) : Message