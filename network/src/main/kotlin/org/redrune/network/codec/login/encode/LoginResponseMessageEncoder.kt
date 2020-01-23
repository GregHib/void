package org.redrune.network.codec.login.encode

import org.redrune.network.codec.login.model.LoginResponseMessage
import org.redrune.network.message.MessageEncoder
import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketBuilder

class LoginResponseMessageEncoder : MessageEncoder<LoginResponseMessage>() {
    override fun encode(message: LoginResponseMessage): Packet {
        return PacketBuilder().writeByte(message.code.opcode).build()
    }
}