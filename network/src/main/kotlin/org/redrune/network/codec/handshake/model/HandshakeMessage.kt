package org.redrune.network.codec.handshake.model

import org.redrune.network.codec.message.Message

data class HandshakeMessage(var response: Int) : Message {

    companion object {

        /**
         * The js5-request opcode.
         */
        const val JS5_REQUEST_OPCODE: Int = 15
        /**
         * The login request opcode.
         */
        const val LOGIN_REQUEST_OPCODE: Int = 14
    }

}