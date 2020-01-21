package org.redrune.network.codec.login.encode

import org.redrune.network.codec.message.MessageEncoder
import org.redrune.network.codec.login.model.LoginResponseMessage
import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketWriter

class LoginResponseMessageEncoder : MessageEncoder<LoginResponseMessage>() {
    override fun encode(message: LoginResponseMessage): Packet {
        val writer = PacketWriter()
        writer.writeByte(message.code.opcode)
        return writer.toPacket()
    }
}