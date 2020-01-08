package org.redrune.network.packet.outgoing

import org.redrune.network.packet.struct.OutgoingPacket
import org.redrune.util.LoginReturnCode

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class LoginResponsePacket(responseCode: LoginReturnCode) : OutgoingPacket() {

    init {
        buffer.writeByte(responseCode.opcode)
    }
}