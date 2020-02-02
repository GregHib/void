package org.redrune.network.codec.handshake.message

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
data class HandshakeRequestMessage(val requestType: HandshakeRequestType, private val data: ByteBuf) :
    DefaultByteBufHolder(data)