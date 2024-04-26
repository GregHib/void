package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.QuickChatPrivate
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class PrivateQuickChatDecoder : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val name = packet.readString()
        val file = packet.readUShort().toInt()
        val data = packet.readBytes(packet.remaining.toInt())
        return QuickChatPrivate(name, file, data)
    }

}