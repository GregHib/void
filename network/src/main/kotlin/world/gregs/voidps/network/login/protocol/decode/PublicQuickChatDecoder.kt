package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.QuickChatPublic
import world.gregs.voidps.network.login.protocol.Decoder

class PublicQuickChatDecoder : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val script = packet.readByte().toInt()
        val file = packet.readUShort().toInt()
        val data = packet.readBytes(packet.remaining.toInt())
        return QuickChatPublic(script, file, data)
    }
}
