package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ChatTypeChange
import world.gregs.voidps.network.login.protocol.Decoder

class ChatSetModeDecoder : Decoder(1) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val x = packet.readUByte().toInt()
        return ChatTypeChange(x)
    }

}