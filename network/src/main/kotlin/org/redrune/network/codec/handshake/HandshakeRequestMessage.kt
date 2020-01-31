package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBufHolder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
data class HandshakeRequestMessage(val requestType: HandshakeRequestType, val bufHolder: ByteBufHolder)