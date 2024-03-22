package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction

class PublicQuickChatDecoder : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val script = packet.readByte().toInt()
        val file = packet.readUShort().toInt()
        val data = packet.readBytes(packet.remaining.toInt())
        instructions.emit(world.gregs.voidps.network.client.instruction.QuickChatPublic(script, file, data))
    }

}