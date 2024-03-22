package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.QuickChatPrivate
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readString

class PrivateQuickChatDecoder : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val name = packet.readString()
        val file = packet.readUShort().toInt()
        val data = packet.readBytes(packet.remaining.toInt())
        instructions.emit(QuickChatPrivate(name, file, data))
    }

}