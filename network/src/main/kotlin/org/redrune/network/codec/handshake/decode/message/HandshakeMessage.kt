package org.redrune.network.codec.handshake.decode.message

import org.redrune.network.codec.message.Message

data class HandshakeMessage(var response: Int) : Message