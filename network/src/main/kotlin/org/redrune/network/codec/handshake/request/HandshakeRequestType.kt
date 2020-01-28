package org.redrune.network.codec.handshake.request

enum class HandshakeRequestType(val opcode: Int) {

    LOGIN(14),

    UPDATE(15),

    CONNECTING_LOGIN(16),

    RECONNECTING_LOGIN(18),

    USER_REGISTRATION(22),

    EMAIL_AVAILABILITY(28),

    ;

    companion object {
        val types = values().associateBy(HandshakeRequestType::opcode)

        fun valueOf(opcode: Int): HandshakeRequestType? {
            return types[opcode]
        }
    }
}